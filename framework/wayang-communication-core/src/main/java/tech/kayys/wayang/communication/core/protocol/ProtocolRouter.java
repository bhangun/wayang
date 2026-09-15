package tech.kayys.wayang.communication.core.protocol;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.exception.NoCompatibleProtocolException;
import tech.kayys.wayang.communication.protocol.ProtocolContext;

import java.util.List;

public interface ProtocolRouter {

    List<ProtocolCandidate> candidates(
            AgentRequest request,
            ProtocolContext context
    );

    default ProtocolCandidate route(
            AgentRequest request,
            ProtocolContext context
    ) {
        List<ProtocolCandidate> list = candidates(request, context);
        if (list.isEmpty()) {
            throw new NoCompatibleProtocolException(request);
        }
        return list.getFirst();
    }
}
