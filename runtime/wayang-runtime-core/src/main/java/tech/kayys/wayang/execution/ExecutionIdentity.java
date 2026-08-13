package tech.kayys.wayang.execution;

import java.util.UUID;

/**
 * Stable identity for an execution.
 */
public record ExecutionIdentity(
    String executionId,
    String correlationId,
    String parentExecutionId
) {
    public static ExecutionIdentity create() {
        return new ExecutionIdentity(
            UUID.randomUUID().toString(),
            UUID.randomUUID().toString(),
            null
        );
    }
}
