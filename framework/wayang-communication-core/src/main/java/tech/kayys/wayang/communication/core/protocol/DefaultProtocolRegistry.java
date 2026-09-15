package tech.kayys.wayang.communication.core.protocol;

import tech.kayys.wayang.communication.protocol.AgentProtocol;
import tech.kayys.wayang.communication.protocol.ProtocolId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultProtocolRegistry
        implements ProtocolRegistry {

    private final Map<ProtocolId, AgentProtocol> protocols =
            new ConcurrentHashMap<>();

    @Override
    public void register(AgentProtocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException(
                    "protocol must not be null"
            );
        }

        protocols.put(protocol.id(), protocol);
    }

    @Override
    public void unregister(ProtocolId protocolId) {
        if (protocolId != null) {
            protocols.remove(protocolId);
        }
    }

    @Override
    public Optional<AgentProtocol> find(
            ProtocolId protocolId
    ) {
        if (protocolId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(
                protocols.get(protocolId)
        );
    }

    @Override
    public Collection<AgentProtocol> protocols() {
        return List.copyOf(protocols.values());
    }

    @Override
    public boolean contains(
            ProtocolId protocolId
    ) {
        return protocolId != null && protocols.containsKey(protocolId);
    }
}
