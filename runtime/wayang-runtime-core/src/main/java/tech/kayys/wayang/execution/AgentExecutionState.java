package tech.kayys.wayang.execution;

import java.time.Instant;

/**
 * Execution state for an agent execution tracking metadata.
 */
public record AgentExecutionState(
    String executionId,
    ExecutionStatus status,
    ExecutionPhase phase,
    int attempt,
    int iteration,
    String checkpointId,
    String lastEventId,
    String modelId,
    long inputTokens,
    long outputTokens,
    Instant startedAt,
    Instant updatedAt
) {
    public AgentExecutionState withPhase(ExecutionPhase newPhase) {
        return new AgentExecutionState(
            executionId, status, newPhase, attempt, iteration,
            checkpointId, lastEventId, modelId, inputTokens, outputTokens,
            startedAt, Instant.now()
        );
    }
}
