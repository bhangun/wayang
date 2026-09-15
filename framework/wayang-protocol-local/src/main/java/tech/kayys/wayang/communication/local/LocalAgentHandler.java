package tech.kayys.wayang.communication.local;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.api.AgentResponse;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface LocalAgentHandler {

    CompletionStage<AgentResponse> handle(
            AgentRequest request
    );
}
