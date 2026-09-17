package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.capability.Capability;

import java.util.List;
import java.util.Objects;

public record CapabilityDiscoveryResult(
        String providerId,
        List<Capability> capabilities
) {

    public CapabilityDiscoveryResult {
        Objects.requireNonNull(providerId, "providerId cannot be null");
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
    }

    public static CapabilityDiscoveryResult of(String providerId, List<Capability> capabilities) {
        return new CapabilityDiscoveryResult(providerId, capabilities);
    }
}
