package tech.kayys.wayang.execution;

import java.util.Optional;
import java.util.UUID;

/**
 * Execution state for a node or graph.
 */
public final class ExecutionState {

    private final UUID executionId;
    private final NodeStatus status;
    private final java.util.Map<UUID, NodeResult> nodeResults;
    private final java.util.Map<String, Object> variables;
    private final long startedAt;
    private final long updatedAt;

    public ExecutionState(UUID executionId) {
        this(executionId, NodeStatus.CREATED, java.util.Map.of(), java.util.Map.of(),
                System.currentTimeMillis(), System.currentTimeMillis());
    }

    public ExecutionState(UUID executionId, NodeStatus status,
            java.util.Map<UUID, NodeResult> nodeResults,
            java.util.Map<String, Object> variables,
            long startedAt, long updatedAt) {
        this.executionId = executionId;
        this.status = status;
        this.nodeResults = nodeResults != null ? java.util.Map.copyOf(nodeResults) : java.util.Map.of();
        this.variables = variables != null ? java.util.Map.copyOf(variables) : java.util.Map.of();
        this.startedAt = startedAt;
        this.updatedAt = updatedAt;
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public NodeStatus getStatus() {
        return status;
    }

    public java.util.Map<UUID, NodeResult> getNodeResults() {
        return nodeResults;
    }

    public java.util.Map<String, Object> getVariables() {
        return variables;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public Optional<NodeResult> getNodeResult(UUID nodeId) {
        return Optional.ofNullable(nodeResults.get(nodeId));
    }

    public ExecutionState withStatus(NodeStatus status) {
        return new ExecutionState(executionId, status, nodeResults, variables, startedAt, System.currentTimeMillis());
    }

    public ExecutionState withNodeResult(UUID nodeId, NodeResult result) {
        java.util.Map<UUID, NodeResult> newResults = new java.util.HashMap<>(nodeResults);
        newResults.put(nodeId, result);
        return new ExecutionState(executionId, status, newResults, variables, startedAt, System.currentTimeMillis());
    }

    public ExecutionState withVariable(String key, Object value) {
        java.util.Map<String, Object> newVariables = new java.util.HashMap<>(variables);
        newVariables.put(key, value);
        return new ExecutionState(executionId, status, nodeResults, newVariables, startedAt,
                System.currentTimeMillis());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID executionId;
        private NodeStatus status = NodeStatus.CREATED;
        private java.util.Map<UUID, NodeResult> nodeResults = java.util.Map.of();
        private java.util.Map<String, Object> variables = java.util.Map.of();
        private long startedAt = System.currentTimeMillis();
        private long updatedAt = System.currentTimeMillis();

        public Builder executionId(UUID executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder status(NodeStatus status) {
            this.status = status;
            return this;
        }

        public Builder nodeResults(java.util.Map<UUID, NodeResult> nodeResults) {
            this.nodeResults = nodeResults;
            return this;
        }

        public Builder variables(java.util.Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public Builder startedAt(long startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder updatedAt(long updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ExecutionState build() {
            return new ExecutionState(executionId, status, nodeResults, variables, startedAt, updatedAt);
        }
    }
}