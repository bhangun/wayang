package tech.kayys.wayang.execution;

import tech.kayys.wayang.agent.AgentResponse;

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

    void checkpoint(AgentExecutionState state);

    void complete(String executionId, AgentResponse response);

    void fail(String executionId, Throwable error);
}
