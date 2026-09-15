package tech.kayys.wayang.communication.exception;

import tech.kayys.wayang.communication.api.AgentRequest;

public final class NoCompatibleProtocolException
        extends CommunicationException {

    public NoCompatibleProtocolException(
            AgentRequest request
    ) {
        super(
                "No compatible protocol for agent: "
                        + (request != null && request.target() != null ? request.target().id() : "unknown")
        );
    }

    public NoCompatibleProtocolException(String message) {
        super(message);
    }
}
