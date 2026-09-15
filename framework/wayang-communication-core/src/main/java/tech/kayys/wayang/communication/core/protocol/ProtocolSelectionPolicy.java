package tech.kayys.wayang.communication.core.protocol;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.protocol.AgentProtocol;
import tech.kayys.wayang.communication.protocol.ProtocolContext;

import java.util.Collection;
import java.util.List;

public interface ProtocolSelectionPolicy {

    List<ProtocolCandidate> rank(
            Collection<AgentProtocol> protocols,
            AgentRequest request,
            ProtocolContext context
    );
}
