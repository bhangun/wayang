package tech.kayys.wayang.communication.local;

import tech.kayys.wayang.communication.api.AgentRef;
import tech.kayys.wayang.communication.endpoint.LocalAgentEndpoint;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultLocalAgentRegistry implements LocalAgentRegistry {

    private final String runtimeId;
    private final Map<String, LocalAgentHandler> handlers = new ConcurrentHashMap<>();

    public DefaultLocalAgentRegistry(String runtimeId) {
        this.runtimeId = Objects.requireNonNull(runtimeId, "runtimeId");
    }

    public DefaultLocalAgentRegistry() {
        this("wayang-runtime-local");
    }

    @Override
    public AgentRef register(
            String agentId,
            String agentName,
            LocalAgentHandler handler
    ) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(handler, "handler");

        handlers.put(agentId, handler);
        return new AgentRef(
                agentId,
                agentName != null ? agentName : agentId,
                new LocalAgentEndpoint(runtimeId)
        );
    }

    @Override
    public void unregister(String agentId) {
        if (agentId != null) {
            handlers.remove(agentId);
        }
    }

    @Override
    public Optional<LocalAgentHandler> find(String agentId) {
        if (agentId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(handlers.get(agentId));
    }

    @Override
    public boolean contains(String agentId) {
        return agentId != null && handlers.containsKey(agentId);
    }
}
