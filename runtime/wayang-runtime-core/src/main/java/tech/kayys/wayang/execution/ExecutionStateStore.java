package tech.kayys.wayang.execution;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.agent.AgentResponse;
import tech.kayys.wayang.execution.lifecycle.ExecutionSemantics;

import java.time.Instant;
import java.util.Map;

/**
 * Execution state transition API.
 */
public interface ExecutionStateStore {

    AgentExecutionState get(String executionId);

    AgentExecutionState transition(
        String executionId,
        ExecutionPhase phase,
        Map<String, Object> metadata
    );

    AgentExecutionState transitionStatus(
        String executionId,
        ExecutionStatus expectedCurrent,
        ExecutionStatus newStatus
    );

    AgentExecutionState changeStatus(
        String executionId,
        ExecutionStatus status
    );

    AgentExecutionState incrementAttempt(
        String executionId
    );

    AgentExecutionState heartbeat(
        String executionId
    );

    AgentExecutionState configure(
        String executionId,
        Instant deadline,
        String idempotencyKey,
        ExecutionSemantics semantics
    );

    ExecutionCheckpoint checkpoint(
        String executionId,
        AgentContext context
    );

    default void checkpoint(AgentExecutionState state) {
        // Deprecated marker
    }

    void complete(String executionId, AgentResponse response);

    void fail(String executionId, Throwable error);
}
