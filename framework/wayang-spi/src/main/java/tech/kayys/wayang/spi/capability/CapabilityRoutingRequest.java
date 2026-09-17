package tech.kayys.wayang.spi.capability;

import java.util.Map;
import java.util.Objects;

/**
 * Request payload submitted to CapabilityRouter.
 */
public record CapabilityRoutingRequest(
        String capabilityId,
        String preferredProviderId,
        CapabilityRoutingStrategy strategy,
        Map<String, Object> criteria
) {

    public CapabilityRoutingRequest {
        Objects.requireNonNull(capabilityId, "capabilityId cannot be null");
        if (capabilityId.isBlank()) {
            throw new IllegalArgumentException("capabilityId cannot be blank");
        }

        strategy = strategy != null ? strategy : CapabilityRoutingStrategy.FIRST_AVAILABLE;
        criteria = criteria == null ? Map.of() : Map.copyOf(criteria);
    }

    public static CapabilityRoutingRequest of(String capabilityId) {
        return new CapabilityRoutingRequest(capabilityId, null, CapabilityRoutingStrategy.FIRST_AVAILABLE, Map.of());
    }

    public static CapabilityRoutingRequest of(String capabilityId, CapabilityRoutingStrategy strategy) {
        return new CapabilityRoutingRequest(capabilityId, null, strategy, Map.of());
    }

    public static CapabilityRoutingRequest direct(String capabilityId, String preferredProviderId) {
        return new CapabilityRoutingRequest(capabilityId, preferredProviderId, CapabilityRoutingStrategy.DIRECT, Map.of());
    }
}
