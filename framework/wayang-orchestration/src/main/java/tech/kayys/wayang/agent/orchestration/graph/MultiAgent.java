package tech.kayys.wayang.agent.orchestration.graph;

import tech.kayys.wayang.agent.orchestration.graph.engine.CompiledWorkflow;
import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;
import tech.kayys.wayang.agent.orchestration.graph.state.StateUpdate;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.spi.agent.Agent;
import tech.kayys.wayang.spi.agent.AgentPipeline;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An Agent implementation backed by a Multi-Agent Graph (CompiledWorkflow).
 */
public class MultiAgent implements Agent {

    private final String id;
    private final Id resourceId;
    private final CompiledWorkflow workflow;

    // Maps HITL taskId → graph threadId so the EventBus listener can resume execution
    private static final java.util.Map<String, String> taskToThreadMap = new ConcurrentHashMap<>();

    public MultiAgent(CompiledWorkflow workflow) {
        this.id = UUID.randomUUID().toString();
        this.resourceId = Id.fromString(this.id);
        this.workflow = workflow;
    }

    // ── Resource / Extension implementation ──────────────────────────────────

    @Override
    public ResourceId id() {
        return new ResourceId.AgentId(resourceId);
    }

    @Override
    public ResourceType type() {
        return new ResourceType.Agent();
    }

    @Override
    public Metadata metadata() {
        return Metadata.empty();
    }

    // ── Agent implementation ──────────────────────────────────────────────────

    @Override
    public String getId() {
        return id;
    }

    @Override
    public AgentPipeline getPipeline() {
        return null; // Not applicable for Graph-based agents
    }

    @Override
    public void initialize() throws Exception {
        // Initialization handled during graph compilation
    }

    @Override
    public Object process(Object request) throws Exception {
        String threadId = UUID.randomUUID().toString();
        GraphState initialState = new GraphState();
        // Seed the initial state using StateUpdate so GraphState.apply() handles it
        initialState.apply(new StateUpdate().put("input", request));

        try {
            GraphState finalState = workflow.invoke(initialState, threadId);
            return finalState.get("output");
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ApprovalRequiredException are) {
                // Map the HITL taskId → graph threadId so the listener can resume it
                taskToThreadMap.put(are.getTaskId(), threadId);
                throw new RuntimeException("Workflow paused for HITL. ThreadId: " + threadId, are);
            }
            throw e;
        }
    }

    @Override
    public Object resume(String threadId) throws Exception {
        // Resume from the stored checkpoint
        try {
            GraphState finalState = workflow.invoke(new GraphState(), threadId);
            return finalState.get("output");
        } catch (RuntimeException e) {
            if (e.getCause() instanceof ApprovalRequiredException) {
                throw new RuntimeException("Workflow paused again for HITL. ThreadId: " + threadId, e.getCause());
            }
            throw e;
        }
    }

    // ── Static helpers for the EventBus resume listener ─────────────────────

    /** Returns the graph threadId associated with a HITL taskId, or null if not found. */
    public static String getThreadIdForTask(String taskId) {
        return taskToThreadMap.get(taskId);
    }

    /** Resumes a paused thread by its threadId (called by VertxAutoResumeListener). */
    public static void resumeThread(String threadId) {
        // In a production system this would look up the MultiAgent instance via a registry.
        // For now we log — the actual resume() is driven by the running workflow's checkpoint.
        java.util.logging.Logger.getLogger(MultiAgent.class.getName())
                .info("Static resumeThread called for threadId: " + threadId
                        + ". Ensure the workflow's CompiledWorkflow is still in scope.");
    }
}
