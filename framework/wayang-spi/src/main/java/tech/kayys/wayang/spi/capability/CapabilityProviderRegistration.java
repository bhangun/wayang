package tech.kayys.wayang.spi.capability;

import java.util.Objects;

/**
 * Registration binding between a capability, its identifier, and its supplying provider.
 */
public record CapabilityProviderRegistration(
        String capabilityId,
        String providerId,
        Capability capability
) {

    public CapabilityProviderRegistration {
        Objects.requireNonNull(capabilityId, "capabilityId cannot be null");
        Objects.requireNonNull(providerId, "providerId cannot be null");
        Objects.requireNonNull(capability, "capability cannot be null");

        if (capabilityId.isBlank()) {
            throw new IllegalArgumentException("capabilityId cannot be blank");
        }

        if (providerId.isBlank()) {
            throw new IllegalArgumentException("providerId cannot be blank");
        }
    }
}
