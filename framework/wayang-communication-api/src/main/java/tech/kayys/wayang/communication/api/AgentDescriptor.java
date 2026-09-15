package tech.kayys.wayang.communication.api;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Canonical, protocol-neutral descriptor for a Wayang agent.
 * This is the single source of truth from which all protocol-specific
 * representations (A2A Agent Card, ANP Agent Description, etc.) are projected.
 */
public record AgentDescriptor(
        AgentRef ref,
        String description,
        List<AgentCapability> capabilities,
        List<AgentEndpointDescriptor> endpoints,
        Map<String, Object> metadata
) {

    public AgentDescriptor {
        Objects.requireNonNull(ref, "ref");

        capabilities = capabilities == null
                ? List.of()
                : List.copyOf(capabilities);

        endpoints = endpoints == null
                ? List.of()
                : List.copyOf(endpoints);

        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);
    }

    public static AgentDescriptor of(AgentRef ref, String description) {
        return new AgentDescriptor(ref, description, List.of(), List.of(), Map.of());
    }

    public static AgentDescriptor of(
            AgentRef ref,
            String description,
            List<AgentCapability> capabilities
    ) {
        return new AgentDescriptor(ref, description, capabilities, List.of(), Map.of());
    }
}
