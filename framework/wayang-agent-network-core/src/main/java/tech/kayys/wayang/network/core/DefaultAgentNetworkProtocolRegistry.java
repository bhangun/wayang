package tech.kayys.wayang.network.core;

import tech.kayys.wayang.network.protocol.AgentNetworkProtocol;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultAgentNetworkProtocolRegistry implements AgentNetworkProtocolRegistry {

    private final ConcurrentMap<String, AgentNetworkProtocol> protocols = new ConcurrentHashMap<>();

    @Override
    public void register(AgentNetworkProtocol protocol) {
        Objects.requireNonNull(protocol, "protocol");
        protocols.put(protocol.id().toUpperCase(), protocol);
    }

    @Override
    public Optional<AgentNetworkProtocol> find(String protocolId) {
        if (protocolId == null) return Optional.empty();
        return Optional.ofNullable(protocols.get(protocolId.toUpperCase()));
    }

    @Override
    public Collection<AgentNetworkProtocol> all() {
        return protocols.values();
    }
}
