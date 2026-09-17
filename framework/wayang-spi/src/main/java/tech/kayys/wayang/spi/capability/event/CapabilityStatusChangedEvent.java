package tech.kayys.wayang.spi.capability.event;

import tech.kayys.wayang.spi.capability.CapabilityProviderStatus;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CapabilityStatusChangedEvent(
        String eventId,
        Instant timestamp,
        String capabilityId,
        String providerId,
        CapabilityProviderStatus previousStatus,
        CapabilityProviderStatus newStatus,
        Map<String, Object> metadata
) implements CapabilityEvent {

    public CapabilityStatusChangedEvent(
            CapabilityProviderStatus previousStatus,
            CapabilityProviderStatus newStatus) {
        this(
                UUID.randomUUID().toString(),
                Instant.now(),
                newStatus.capabilityId(),
                newStatus.providerId(),
                previousStatus,
                Objects.requireNonNull(newStatus, "newStatus is required"),
                Map.of()
        );
    }
}
