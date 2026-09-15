package tech.kayys.wayang.network.core;

import tech.kayys.wayang.network.protocol.AgentNetworkProtocol;

import java.util.Collection;
import java.util.Optional;

public interface AgentNetworkProtocolRegistry {

    void register(AgentNetworkProtocol protocol);

    Optional<AgentNetworkProtocol> find(String protocolId);

    Collection<AgentNetworkProtocol> all();
}
