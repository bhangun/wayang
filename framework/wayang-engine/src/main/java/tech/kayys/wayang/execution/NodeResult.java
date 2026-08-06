package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Result of a node execution.
 */
public final class NodeResult {

    private final UUID nodeId;
    private final boolean success;
    private final Object output;
    private final String errorMessage;
    private final long executionTimeMs;
    private final Map<String, Object> metadata;

    public NodeResult(UUID nodeId, boolean success, Object output, String errorMessage,
            long executionTimeMs, Map<String, Object> metadata) {
        this.nodeId = nodeId;
        this.success = success;
        this.output = output;
        this.errorMessage = errorMessage;
        this.executionTimeMs = executionTimeMs;
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public UUID getNodeId() {
        return nodeId;
    }

    public boolean isSuccess() {
        return success;
    }

    public Object getOutput() {
        return output;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public static NodeResult success(UUID nodeId) {
        return new NodeResult(nodeId, true, null, null, 0, Map.of());
    }

    public static NodeResult success(UUID nodeId, Object output) {
        return new NodeResult(nodeId, true, output, null, 0, Map.of());
    }

    public static NodeResult success(UUID nodeId, Object output, Map<String, Object> metadata) {
        return new NodeResult(nodeId, true, output, null, 0, metadata);
    }

    public static NodeResult failure(UUID nodeId, String errorMessage) {
        return new NodeResult(nodeId, false, null, errorMessage, 0, Map.of());
    }

    public static NodeResult failure(UUID nodeId, String errorMessage, Map<String, Object> metadata) {
        return new NodeResult(nodeId, false, null, errorMessage, 0, metadata);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID nodeId;
        private boolean success;
        private Object output;
        private String errorMessage;
        private long executionTimeMs;
        private Map<String, Object> metadata;

        public Builder nodeId(UUID nodeId) {
            this.nodeId = nodeId;
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder output(Object output) {
            this.output = output;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public NodeResult build() {
            return new NodeResult(nodeId, success, output, errorMessage, executionTimeMs, metadata);
        }
    }
}
