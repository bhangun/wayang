package tech.kayys.wayang.a2a.descriptor;

import tech.kayys.wayang.communication.api.AgentDescriptor;

import java.util.Objects;

/**
 * Runtime service that generates an {@link A2AAgentCard} from the live {@link AgentDescriptor}.
 * <p>
 * Can be registered as a CDI bean or used directly in Quarkus resources.
 */
public class A2AAgentCardService {

    private final AgentDescriptor descriptor;
    private final String publicUrl;
    private final A2ACapabilityMapper capabilityMapper;
    private final boolean exposeMetadata;

    public A2AAgentCardService(AgentDescriptor descriptor, String publicUrl) {
        this(descriptor, publicUrl, new DefaultA2ACapabilityMapper(), false);
    }

    public A2AAgentCardService(
            AgentDescriptor descriptor,
            String publicUrl,
            A2ACapabilityMapper capabilityMapper,
            boolean exposeMetadata
    ) {
        this.descriptor       = Objects.requireNonNull(descriptor, "descriptor");
        this.publicUrl        = publicUrl;
        this.capabilityMapper = Objects.requireNonNull(capabilityMapper, "capabilityMapper");
        this.exposeMetadata   = exposeMetadata;
    }

    /**
     * Generates and returns the Agent Card from the current descriptor state.
     * This is intentionally not cached — the card reflects live state.
     */
    public A2AAgentCard card() {
        return A2AAgentCardMapper.fromWayang(descriptor, publicUrl, capabilityMapper, exposeMetadata);
    }
}
