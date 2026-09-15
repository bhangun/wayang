package tech.kayys.wayang.execution;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Immutable metadata associated with an agent execution.
 */
public record ExecutionMetadata(
        String executionId,
        String parentExecutionId,
        String traceId,
        Instant startedAt,
        Map<String, Object> attributes
) {
    public ExecutionMetadata {
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(startedAt, "startedAt must not be null");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public Optional<String> optParentExecutionId() {
        return Optional.ofNullable(parentExecutionId);
    }

    public Optional<String> optTraceId() {
        return Optional.ofNullable(traceId);
    }
}
