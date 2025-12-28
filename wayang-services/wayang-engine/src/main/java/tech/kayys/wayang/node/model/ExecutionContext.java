package tech.kayys.wayang.node.model;

import lombok.Builder;
import lombok.Data;
import tech.kayys.wayang.common.spi.Guardrails;
import tech.kayys.wayang.common.spi.NodeContext;
import tech.kayys.wayang.common.spi.ProvenanceContext;
import tech.kayys.wayang.schema.ExecutionError;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExecutionContext - Immutable snapshot of workflow execution state.
 */
@Data
@Builder(toBuilder = true)
public class ExecutionContext {

    private final String runId;
    private final String workflowId;
    private final String tenantId;
    private final Object workflow;
    private final Instant startTime;

    // Input/Output state
    private final Map<String, Object> initialInputs;
    @Builder.Default
    private final Map<String, Object> outputs = new ConcurrentHashMap<>();

    // Node execution tracking
    @Builder.Default
    private final Map<String, NodeExecutionResult> nodeResults = new ConcurrentHashMap<>();

    @Builder.Default
    private final Map<String, NodeState> nodeStates = new ConcurrentHashMap<>();

    // Data flow between nodes (keyed by edge id or port mapping)
    @Builder.Default
    private final Map<String, Object> dataFlow = new ConcurrentHashMap<>();

    // Error tracking
    @Builder.Default
    private final List<ExecutionError> errorHistory = new ArrayList<>();

    // HITL state
    private volatile boolean awaitingHuman;
    private volatile String humanTaskId;
    @Builder.Default
    private final List<HTILTaskResult> humanDecisions = new ArrayList<>();

    // Metadata for extensions (agents, memory, etc.)
    @Builder.Default
    private final Map<String, Object> metadata = new ConcurrentHashMap<>();

    // Performance tracking
    @Builder.Default
    private final Map<String, Long> nodeDurations = new ConcurrentHashMap<>();

    /**
     * Create initial execution context from workflow run.
     */
    public static ExecutionContext create(WorkflowRun run,
            tech.kayys.wayang.schema.workflow.WorkflowDefinition workflow) {
        return ExecutionContext.builder()
                .runId(run.getId())
                .workflowId(workflow.getId().toString())
                .tenantId(run.getTenantId())
                .workflow(workflow)
                .startTime(run.getStartTime())
                .initialInputs(run.getInputs())
                .build();
    }

    /**
     * Create node-specific context for execution.
     */
    public NodeContext createNodeContext(Object nodeDef) {
        String nodeId = extractNodeId(nodeDef);
        Map<String, Object> inputs = resolveNodeInputs(nodeDef);

        return new SimpleNodeContext(
                nodeId,
                runId,
                tenantId,
                inputs,
                new HashMap<>(metadata),
                (tech.kayys.wayang.schema.workflow.WorkflowDefinition) workflow);
    }

    public String getExecutionId() {
        return runId;
    }

    public long getExecutionDuration() {
        if (startTime == null)
            return 0;
        return Duration.between(startTime, Instant.now()).toMillis();
    }

    /**
     * Placeholder trace class to satisfy AgentWebSocket for now.
     */
    public static class ExecutionTrace {
        public String nodeId;
        public String status;
        public String timestamp;
    }

    public List<ExecutionTrace> getExecutionTrace() {
        return new ArrayList<>();
    }

    // Previous helper methods...
    private String extractNodeId(Object nodeDef) {
        return "node-id"; // Stub
    }

    private Map<String, Object> resolveNodeInputs(Object nodeDef) {
        return new HashMap<>(); // Stub
    }

    /**
     * Simple implementation of NodeContext interface for use in execution.
     */
    public static class SimpleNodeContext implements NodeContext {
        private final String nodeId;
        private final java.util.UUID runId;
        private final String tenantId;
        private final Map<String, Object> inputs;
        private final Map<String, Object> metadata;
        private final tech.kayys.wayang.schema.workflow.WorkflowDefinition workflow;
        private final Map<String, Object> workflowState;

        public SimpleNodeContext(String nodeId, String runId, String tenantId,
                Map<String, Object> inputs, Map<String, Object> metadata,
                tech.kayys.wayang.schema.workflow.WorkflowDefinition workflow) {
            this.nodeId = nodeId;
            this.runId = java.util.UUID.fromString(runId != null ? runId : "00000000-0000-0000-0000-000000000000");
            this.tenantId = tenantId;
            this.inputs = inputs != null ? new HashMap<>(inputs) : new HashMap<>();
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.workflow = workflow;
            this.workflowState = new HashMap<>();
        }

        @Override
        public String getNodeId() {
            return nodeId;
        }

        @Override
        public java.util.UUID getRunId() {
            return runId;
        }

        @Override
        public String getTenantId() {
            return tenantId;
        }

        @Override
        public <T> T getInput(String portName, Class<T> type) {
            return type.cast(inputs.get(portName));
        }

        @Override
        public Object getInput(String portName) {
            return inputs.get(portName);
        }

        @Override
        public Guardrails getGuardrails() {
            return null;
        }

        @Override
        public ProvenanceContext getProvenance() {
            return null;
        }

        @Override
        public Map<String, Object> getWorkflowState() {
            return workflowState;
        }

        @Override
        public void storeIntermediateState(Map<String, Object> state) {
            workflowState.putAll(state);
        }
    }
}
