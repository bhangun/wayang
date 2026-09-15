package tech.kayys.wayang.communication.core.protocol;

import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.protocol.ProtocolContext;

import java.util.List;
import java.util.Objects;

public final class DefaultProtocolRouter
        implements ProtocolRouter {

    private final ProtocolRegistry registry;
    private final ProtocolSelectionPolicy policy;

    public DefaultProtocolRouter(
            ProtocolRegistry registry,
            ProtocolSelectionPolicy policy
    ) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    public DefaultProtocolRouter(ProtocolRegistry registry) {
        this(registry, new DefaultProtocolSelectionPolicy());
    }

    @Override
    public List<ProtocolCandidate> candidates(
            AgentRequest request,
            ProtocolContext context
    ) {
        return policy.rank(
                registry.protocols(),
                request,
                context
        );
    }
}
