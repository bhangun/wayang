package tech.kayys.wayang.execution.lifecycle;

import tech.kayys.wayang.execution.ExecutionStatus;

import java.time.Instant;

public record ExecutionLifecycle(
    String executionId,
    ExecutionStatus status,
    int attempt,
    Instant updatedAt
) {
}
