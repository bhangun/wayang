package tech.kayys.wayang.execution;

import tech.kayys.wayang.execution.lifecycle.ExecutionSemantics;

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
    Instant updatedAt,

    Instant deadline,
    Instant lastHeartbeatAt,

    String idempotencyKey,
    ExecutionSemantics executionSemantics
) {

    public AgentExecutionState(
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
        this(executionId, status, phase, attempt, iteration, checkpointId, lastEventId, modelId, inputTokens, outputTokens, startedAt, updatedAt, null, null, null, ExecutionSemantics.AT_LEAST_ONCE);
    }

    public AgentExecutionState withPhase(ExecutionPhase newPhase) {
        return new AgentExecutionState(
            executionId,
            status,
            newPhase,
            attempt,
            iteration,
            checkpointId,
            lastEventId,
            modelId,
            inputTokens,
            outputTokens,
            startedAt,
            Instant.now(),
            deadline,
            lastHeartbeatAt,
            idempotencyKey,
            executionSemantics
        );
    }

    public AgentExecutionState withStatus(ExecutionStatus newStatus) {
        return new AgentExecutionState(
            executionId,
            newStatus,
            phase,
            attempt,
            iteration,
            checkpointId,
            lastEventId,
            modelId,
            inputTokens,
            outputTokens,
            startedAt,
            Instant.now(),
            deadline,
            lastHeartbeatAt,
            idempotencyKey,
            executionSemantics
        );
    }

    public AgentExecutionState withAttempt(int newAttempt) {
        return new AgentExecutionState(
            executionId,
            status,
            phase,
            newAttempt,
            iteration,
            checkpointId,
            lastEventId,
            modelId,
            inputTokens,
            outputTokens,
            startedAt,
            Instant.now(),
            deadline,
            lastHeartbeatAt,
            idempotencyKey,
            executionSemantics
        );
    }

    public AgentExecutionState heartbeat() {
        return new AgentExecutionState(
            executionId,
            status,
            phase,
            attempt,
            iteration,
            checkpointId,
            lastEventId,
            modelId,
            inputTokens,
            outputTokens,
            startedAt,
            updatedAt,
            deadline,
            Instant.now(),
            idempotencyKey,
            executionSemantics
        );
    }
}
