package tech.kayys.wayang.spi.capability.event;

import tech.kayys.wayang.spi.capability.CapabilityDescriptor;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CapabilityRegisteredEvent(
        String eventId,
        Instant timestamp,
        String capabilityId,
        String providerId,
        CapabilityDescriptor descriptor,
        Map<String, Object> metadata
) implements CapabilityEvent {

    public CapabilityRegisteredEvent(
            String capabilityId,
            String providerId,
            CapabilityDescriptor descriptor) {
        this(
                UUID.randomUUID().toString(),
                Instant.now(),
                Objects.requireNonNull(capabilityId, "capabilityId is required"),
                Objects.requireNonNull(providerId, "providerId is required"),
                Objects.requireNonNull(descriptor, "descriptor is required"),
                Map.of()
        );
    }
}
