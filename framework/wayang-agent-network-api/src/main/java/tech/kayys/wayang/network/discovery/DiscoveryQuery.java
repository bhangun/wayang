package tech.kayys.wayang.network.discovery;

import java.util.Map;

public record DiscoveryQuery(
        String capability,
        Map<String, Object> filters
) {

    public DiscoveryQuery {
        filters = filters == null ? Map.of() : Map.copyOf(filters);
    }

    public static DiscoveryQuery forCapability(String capability) {
        return new DiscoveryQuery(capability, Map.of());
    }
}
