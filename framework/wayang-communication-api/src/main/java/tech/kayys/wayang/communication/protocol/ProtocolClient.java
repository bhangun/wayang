package tech.kayys.wayang.communication.protocol;

import tech.kayys.wayang.communication.api.AgentEvent;
import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.api.AgentResponse;
import tech.kayys.wayang.communication.api.AgentTask;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public interface ProtocolClient {

    CompletionStage<AgentResponse> send(
            AgentRequest request,
            ProtocolContext context
    );

    AgentTask submit(
            AgentRequest request,
            ProtocolContext context
    );

    Flow.Publisher<AgentEvent> stream(
            AgentRequest request,
            ProtocolContext context
    );
}
