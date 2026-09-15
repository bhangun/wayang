package tech.kayys.wayang.communication.protocol;

import tech.kayys.wayang.communication.api.AgentMessage;

import java.util.concurrent.CompletionStage;

public interface ProtocolServer {

    CompletionStage<AgentMessage> receive(
            AgentMessage message,
            ProtocolContext context
    );
}
