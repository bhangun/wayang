package tech.kayys.wayang.spi.capability.event;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CapabilityUnregisteredEvent(
        String eventId,
        Instant timestamp,
        String capabilityId,
        String providerId,
        String reason,
        Map<String, Object> metadata
) implements CapabilityEvent {

    public CapabilityUnregisteredEvent(
            String capabilityId,
            String providerId,
            String reason) {
        this(
                UUID.randomUUID().toString(),
                Instant.now(),
                Objects.requireNonNull(capabilityId, "capabilityId is required"),
                Objects.requireNonNull(providerId, "providerId is required"),
                reason,
                Map.of()
        );
    }
}
