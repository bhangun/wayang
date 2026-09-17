package tech.kayys.wayang.spi.capability;

import java.util.Objects;

/**
 * Declares a dependency or requirement on a specific capability.
 */
public record CapabilityRequirement(
        String capabilityId,
        boolean required
) {

    public CapabilityRequirement {
        Objects.requireNonNull(capabilityId, "capabilityId cannot be null");
        if (capabilityId.isBlank()) {
            throw new IllegalArgumentException("capabilityId cannot be blank");
        }
    }

    public static CapabilityRequirement required(String capabilityId) {
        return new CapabilityRequirement(capabilityId, true);
    }

    public static CapabilityRequirement optional(String capabilityId) {
        return new CapabilityRequirement(capabilityId, false);
    }
}
