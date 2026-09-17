package tech.kayys.wayang.spi.capability.event;

import tech.kayys.wayang.spi.capability.CapabilityRoutingStrategy;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CapabilityRoutedEvent(
        String eventId,
        Instant timestamp,
        String capabilityId,
        String providerId,
        CapabilityRoutingStrategy strategy,
        int candidateCount,
        Map<String, Object> metadata
) implements CapabilityEvent {

    public CapabilityRoutedEvent(
            String capabilityId,
            String providerId,
            CapabilityRoutingStrategy strategy,
            int candidateCount) {
        this(
                UUID.randomUUID().toString(),
                Instant.now(),
                Objects.requireNonNull(capabilityId, "capabilityId is required"),
                Objects.requireNonNull(providerId, "providerId is required"),
                strategy,
                candidateCount,
                Map.of()
        );
    }
}
