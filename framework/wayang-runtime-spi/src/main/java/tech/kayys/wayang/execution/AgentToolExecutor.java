package tech.kayys.wayang.execution;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * Pipeline for executing tools with policies.
 *
 * <p>Supports both sequential and parallel execution. When a model emits
 * multiple independent tool calls in a single turn, {@link #executeAll} runs
 * them concurrently and merges the decisions.</p>
 */
public interface AgentToolExecutor {

    /**
     * Evaluates and executes a single tool invocation.
     * May return {@link AgentDecision.WaitForApproval} or {@link AgentDecision.ExecuteTool}.
     *
     * @param invocation The requested tool invocation
     * @return Future containing the decision
     */
    CompletionStage<AgentDecision> execute(ToolInvocation invocation);

    /**
     * Executes multiple independent tool invocations in parallel.
     * If any invocation requires approval, the whole batch pauses.
     *
     * @param invocations List of independent tool invocations
     * @return Future containing a list of decisions (one per invocation, in order)
     */
    default CompletionStage<List<AgentDecision>> executeAll(List<ToolInvocation> invocations) {
        List<CompletableFuture<AgentDecision>> futures = invocations.stream()
            .map(inv -> execute(inv).toCompletableFuture())
            .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }

    /**
     * Optional interface that {@link AgentToolExecutor} implementations can implement
     * to expose the set of {@link Tool} instances they manage.
     *
     * <p>{@link DefaultAgentExecution} checks for this interface at execution time so
     * it can pass the registered tools to the underlying {@code ReActAgent} without a
     * direct dependency on the concrete executor class.</p>
     */
    interface ToolAware {
        /** Returns all tools that this executor can dispatch. Never {@code null}. */
        List<Tool> availableTools();
    }
}

