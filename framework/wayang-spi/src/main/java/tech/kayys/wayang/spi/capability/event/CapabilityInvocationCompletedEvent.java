package tech.kayys.wayang.spi.capability.event;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public record CapabilityInvocationCompletedEvent(
        String eventId,
        Instant timestamp,
        String capabilityId,
        String providerId,
        String invocationId,
        long durationMillis,
        boolean success,
        String errorMessage,
        Map<String, Object> metadata
) implements CapabilityEvent {

    public CapabilityInvocationCompletedEvent(
            String capabilityId,
            String providerId,
            String invocationId,
            long durationMillis,
            boolean success,
            String errorMessage) {
        this(
                UUID.randomUUID().toString(),
                Instant.now(),
                Objects.requireNonNull(capabilityId, "capabilityId is required"),
                Objects.requireNonNull(providerId, "providerId is required"),
                Objects.requireNonNull(invocationId, "invocationId is required"),
                durationMillis,
                success,
                errorMessage,
                Map.of()
        );
    }
}
