package tech.kayys.wayang.execution;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Result of an execution.
 */
public final class ExecutionResult {

    private final UUID executionId;
    private final UUID graphId;
    private final ExecutionStatus status;
    private final Object result;
    private final String errorMessage;
    private final Instant startTime;
    private final Instant endTime;
    private final Duration duration;
    private final Map<String, Object> metrics;
    private final Map<String, Object> metadata;

    public ExecutionResult(UUID executionId, UUID graphId, ExecutionStatus status,
            Object result, String errorMessage, Instant startTime,
            Instant endTime, Map<String, Object> metrics,
            Map<String, Object> metadata) {
        this.executionId = executionId;
        this.graphId = graphId;
        this.status = status;
        this.result = result;
        this.errorMessage = errorMessage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = startTime != null && endTime != null ? Duration.between(startTime, endTime) : Duration.ZERO;
        this.metrics = metrics != null ? Map.copyOf(metrics) : Map.of();
        this.metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }

    public UUID getExecutionId() {
        return executionId;
    }

    public UUID getGraphId() {
        return graphId;
    }

    public ExecutionStatus getStatus() {
        return status;
    }

    public Object getResult() {
        return result;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public Map<String, Object> getMetrics() {
        return metrics;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isSuccess() {
        return status == ExecutionStatus.COMPLETED;
    }

    public boolean isFailure() {
        return status == ExecutionStatus.FAILED || status == ExecutionStatus.ERROR;
    }

    public <T> Optional<T> getResult(Class<T> type) {
        if (result != null && type.isInstance(result)) {
            return Optional.of(type.cast(result));
        }
        return Optional.empty();
    }

    public static ExecutionResult success(UUID executionId, Object result) {
        Instant now = Instant.now();
        return new ExecutionResult(executionId, null, ExecutionStatus.COMPLETED,
                result, null, now, now, Map.of(), Map.of());
    }

    public static ExecutionResult failure(UUID executionId, String errorMessage) {
        Instant now = Instant.now();
        return new ExecutionResult(executionId, null, ExecutionStatus.FAILED,
                null, errorMessage, now, now, Map.of(), Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID executionId;
        private UUID graphId;
        private ExecutionStatus status;
        private Object result;
        private String errorMessage;
        private Instant startTime;
        private Instant endTime;
        private Map<String, Object> metrics;
        private Map<String, Object> metadata;

        public Builder executionId(UUID executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder graphId(UUID graphId) {
            this.graphId = graphId;
            return this;
        }

        public Builder status(ExecutionStatus status) {
            this.status = status;
            return this;
        }

        public Builder result(Object result) {
            this.result = result;
            return this;
        }

        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder metrics(Map<String, Object> metrics) {
            this.metrics = metrics;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ExecutionResult build() {
            return new ExecutionResult(executionId, graphId, status, result, errorMessage,
                    startTime, endTime, metrics, metadata);
        }
    }
}
