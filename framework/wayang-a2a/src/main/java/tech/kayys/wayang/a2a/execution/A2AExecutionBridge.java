package tech.kayys.wayang.a2a.execution;

import tech.kayys.wayang.communication.api.AgentResponse;

import java.util.concurrent.CompletionStage;

/**
 * Protocol/Execution Bridge between A2A transport and Wayang execution layer.
 */
public interface A2AExecutionBridge {

    CompletionStage<AgentResponse> execute(A2AExecutionRequest request);

    CompletionStage<A2ATaskExecution> submit(A2AExecutionRequest request);
}
