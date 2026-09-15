package tech.kayys.wayang.network.discovery;

import tech.kayys.wayang.communication.api.AgentDescriptor;

import java.util.List;

public record DiscoveryResult(
        List<AgentDescriptor> agents
) {

    public DiscoveryResult {
        agents = agents == null ? List.of() : List.copyOf(agents);
    }

    public static DiscoveryResult of(List<AgentDescriptor> agents) {
        return new DiscoveryResult(agents);
    }
}
