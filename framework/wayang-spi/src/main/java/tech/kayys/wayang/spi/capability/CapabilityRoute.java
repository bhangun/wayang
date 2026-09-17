package tech.kayys.wayang.spi.capability;

import java.util.Objects;

/**
 * Resolved route binding a capability to a selected provider instance.
 */
public record CapabilityRoute(
        String capabilityId,
        String providerId,
        Capability capability
) {

    public CapabilityRoute {
        Objects.requireNonNull(capabilityId, "capabilityId cannot be null");
        Objects.requireNonNull(providerId, "providerId cannot be null");
        Objects.requireNonNull(capability, "capability cannot be null");
    }
}
