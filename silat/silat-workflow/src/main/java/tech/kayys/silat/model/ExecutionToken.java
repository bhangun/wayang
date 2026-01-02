package tech.kayys.silat.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Execution Token - Security token for node execution
 * Ensures only authorized executors can report results
 */
public record ExecutionToken(
        String value,
        WorkflowRunId runId,
        NodeId nodeId,
        int attempt,
        Instant expiresAt) {
    public ExecutionToken {
        Objects.requireNonNull(value, "Token value cannot be null");
        Objects.requireNonNull(runId, "RunId cannot be null");
        Objects.requireNonNull(nodeId, "NodeId cannot be null");
        Objects.requireNonNull(expiresAt, "ExpiresAt cannot be null");
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isExpired();
    }

    public static ExecutionToken create(WorkflowRunId runId, NodeId nodeId, int attempt, Duration validity) {
        return new ExecutionToken(
                UUID.randomUUID().toString(),
                runId,
                nodeId,
                attempt,
                Instant.now().plus(validity));
    }
}