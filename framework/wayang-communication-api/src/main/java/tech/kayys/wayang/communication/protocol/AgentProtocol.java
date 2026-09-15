package tech.kayys.wayang.communication.protocol;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.capability.ProtocolCapability;
import tech.kayys.wayang.communication.endpoint.AgentEndpoint;

import java.util.Set;

public interface AgentProtocol {

    ProtocolId id();

    ProtocolVersion version();

    Set<ProtocolCapability> capabilities();

    boolean supports(
            AgentEndpoint endpoint,
            AgentRequest request
    );

    ProtocolClient client(
            ProtocolContext context
    );

    default ProtocolServer server() {
        return null;
    }
}
