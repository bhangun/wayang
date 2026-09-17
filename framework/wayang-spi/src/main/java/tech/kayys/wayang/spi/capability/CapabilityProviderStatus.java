package tech.kayys.wayang.spi.capability;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record CapabilityProviderStatus(
        String capabilityId,
        String providerId,
        CapabilityAvailability availability,
        CapabilityHealth health,
        Instant checkedAt,
        Long latencyMillis,
        Double load,
        String message,
        Map<String, Object> attributes
) {

    public CapabilityProviderStatus {
        if (capabilityId == null || capabilityId.isBlank()) {
            throw new IllegalArgumentException("capabilityId cannot be null or blank");
        }

        if (providerId == null || providerId.isBlank()) {
            throw new IllegalArgumentException("providerId cannot be null or blank");
        }

        availability = Objects.requireNonNull(availability, "availability cannot be null");
        health = Objects.requireNonNull(health, "health cannot be null");

        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);

        if (latencyMillis != null && latencyMillis < 0) {
            throw new IllegalArgumentException("latencyMillis cannot be negative");
        }

        if (load != null && (load < 0.0 || load > 1.0)) {
            throw new IllegalArgumentException("load must be between 0.0 and 1.0");
        }
    }

    public boolean available() {
        return availability == CapabilityAvailability.AVAILABLE;
    }

    public boolean healthy() {
        return health == CapabilityHealth.HEALTHY;
    }

    public boolean usable() {
        return available() && health != CapabilityHealth.UNHEALTHY;
    }
}
