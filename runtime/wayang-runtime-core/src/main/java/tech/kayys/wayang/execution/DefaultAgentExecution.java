package tech.kayys.wayang.execution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.agent.AgentRequest;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.agent.PermissionDecision;
import tech.kayys.wayang.agent.WayangAgentListener;
import tech.kayys.wayang.agent.builder.AgentBuilder;
import tech.kayys.wayang.core.AgentDefinition;
import tech.kayys.wayang.json.JsonValue;
import tech.kayys.wayang.provider.Provider;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolResult;

/**
 * Default implementation of {@link AgentExecution}.
 *
 * <p>Phase 4 enhancement: instead of the Phase 1 stub response, this class now
 * builds a real {@code ReActAgent} from the {@link AgentDefinition}, resolves a
 * {@link Provider}, and drives the full ReAct loop asynchronously.  The streaming
 * callbacks are bridged into a {@link CompletableFuture} that is returned to the
 * caller.</p>
 */
public class DefaultAgentExecution implements AgentExecution {

    private static final Executor AGENT_POOL =
        Executors.newVirtualThreadPerTaskExecutor();

    private final String id;
    private final AgentDefinition agent;
    private final AgentContext agentContext;
    private final ExecutionBudget budget;
    private final CheckpointStore checkpointStore;
    private final AgentToolExecutor toolExecutor;
    /** Providers resolved from CDI — may be empty when running without a provider. */
    private final List<Provider> providers;

    private volatile ExecutionStatus status;

    public DefaultAgentExecution(
        String id,
        AgentDefinition agent,
        AgentContext agentContext,
        
        ExecutionBudget budget,
        CheckpointStore checkpointStore,
        AgentToolExecutor toolExecutor,
        List<Provider> providers
    ) {
        this.id = id;
        this.agent = agent;
        this.agentContext = agentContext;
        
        this.budget = budget;
        this.checkpointStore = checkpointStore;
        this.toolExecutor = toolExecutor;
        this.providers = providers != null ? new ArrayList<>(providers) : new ArrayList<>();
        this.status = ExecutionStatus.PENDING;
    }

    /** Backwards-compatible constructor without providers (integration tests, resume). */
    public DefaultAgentExecution(
        String id,
        AgentDefinition agent,
        AgentContext agentContext,
        
        ExecutionBudget budget,
        CheckpointStore checkpointStore,
        AgentToolExecutor toolExecutor
    ) {
        this(id, agent, agentContext, budget,
             checkpointStore, toolExecutor, List.of());
    }

    @Override
    public String id() { return id; }

    @Override
    public AgentContext agentContext() { return agentContext; }

    @Override
    public ExecutionStatus status() { return status; }

    // -------------------------------------------------------------------------
    // Core execution — drives the ReAct loop
    // -------------------------------------------------------------------------

    @Override
    public CompletionStage<AgentResponse> execute() {
        this.status = ExecutionStatus.RUNNING;
        CompletableFuture<AgentResponse> future = new CompletableFuture<>();

        AGENT_POOL.execute(() -> {
            try {
                runAgentLoop(future);
            } catch (Throwable t) {
                this.status = ExecutionStatus.FAILED;
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    private void runAgentLoop(CompletableFuture<AgentResponse> future) {
        // Resolve provider — fall back to a no-op execution when none is wired.
        if (providers.isEmpty()) {
            stubComplete(future, "No provider configured; returning stub response.");
            return;
        }

        Provider provider = providers.get(0);

        // Derive the prompt from AgentRequest or fall back to empty string.
        AgentRequest request = agentContext.request();
        String prompt = (request != null && request.content() != null)
            ? request.content()
            : "";

        if (prompt.isBlank()) {
            stubComplete(future, "Empty prompt; returning stub response.");
            return;
        }

        // Resolve tools from the tool executor if available.
        List<Tool> tools = new ArrayList<>();
        if (toolExecutor instanceof AgentToolExecutor.ToolAware ta) {
            tools.addAll(ta.availableTools());
        }

        // Extract optional system prompt from agent metadata.
        String systemPrompt = (agent != null && agent.goal() != null)
            ? agent.goal()
            : "You are a helpful AI assistant.";

        // Build the ReActAgent.
        Agent reactAgent = AgentBuilder.create("react")
            .withProvider(provider)
            .withSystemPrompt(systemPrompt)
            .withTools(tools)
            .withAutoApproveTools(true)
            .build();

        // Buffer accumulated text for the final response content.
        StringBuilder contentBuffer = new StringBuilder();

        // Drive the loop via WayangAgentListener callbacks.
        reactAgent.send(prompt, new WayangAgentListener() {

            @Override
            public void onTextDelta(String text) {
                contentBuffer.append(text);
            }

            @Override
            public void onToolCallStart(String callId, String name) {
                // No buffering needed — tool execution handled inside ReActAgent.
            }

            @Override
            public void onToolCallReady(String callId, String name, JsonValue input) {
                // Visibility hook — no action required here.
            }

            @Override
            public void onToolPermissionNeeded(String callId, String name,
                    JsonValue input, java.util.function.Consumer<PermissionDecision> responder) {
                // Auto-approve all tools in headless/CI mode.
                responder.accept(PermissionDecision.APPROVE_ONCE);
            }

            @Override
            public void onToolResult(String callId, String name, ToolResult result) {
                // Visibility hook — the ReActAgent has already appended the result to history.
            }

            @Override
            public void onUsage(int inputTokens, int outputTokens) {
                // Could record into AgentContext metrics in a future pass.
            }

            @Override
            public void onDone(String stopReason) {
                DefaultAgentExecution.this.status = ExecutionStatus.COMPLETED;
                checkpointStore.save(id, agentContext);

                AgentResponse response = AgentResponse.builder()
                    .id(id)
                    .success(true)
                    .content(contentBuffer.toString())
                    .metadata("stopReason", stopReason)
                    .build();

                future.complete(response);
            }

            @Override
            public void onError(String message) {
                DefaultAgentExecution.this.status = ExecutionStatus.FAILED;

                AgentResponse response = AgentResponse.builder()
                    .id(id)
                    .success(false)
                    .error(message)
                    .build();

                future.complete(response);
            }
        });
    }

    /** Returns a completed stub response — used when a real execution cannot be performed. */
    private void stubComplete(CompletableFuture<AgentResponse> future, String reason) {
        this.status = ExecutionStatus.COMPLETED;
        checkpointStore.save(id, agentContext);
        future.complete(AgentResponse.builder()
            .id(id)
            .success(true)
            .content(reason)
            .build());
    }

    // -------------------------------------------------------------------------
    // Synchronous convenience
    // -------------------------------------------------------------------------

    @Override
    public AgentResponse executeSync() {
        try {
            return execute().toCompletableFuture().join();
        } catch (Exception e) {
            this.status = ExecutionStatus.FAILED;
            return AgentResponse.builder()
                .id(id)
                .success(false)
                .error(e.getMessage())
                .build();
        }
    }

    @Override
    public AgentResponse join() {
        if (status == ExecutionStatus.COMPLETED
                || status == ExecutionStatus.FAILED
                || status == ExecutionStatus.CANCELLED) {
            return AgentResponse.builder()
                .id(id)
                .success(status == ExecutionStatus.COMPLETED)
                .build();
        }
        return executeSync();
    }

    // -------------------------------------------------------------------------
    // Lifecycle controls
    // -------------------------------------------------------------------------

    @Override
    public void pause() {
        this.status = ExecutionStatus.PAUSED;
        checkpointStore.save(id, agentContext);
    }

    @Override
    public void resume() {
        this.status = ExecutionStatus.RUNNING;
    }

    @Override
    public void cancel() {
        this.status = ExecutionStatus.CANCELLED;
        checkpointStore.delete(id);
    }
}
