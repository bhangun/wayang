package tech.kayys.wayang.spi.capability;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable descriptor declaring the contract, semantics, and attributes of a capability.
 */
public record CapabilityDescriptor(
        String id,
        String description,
        CapabilityType type,
        Map<String, Object> attributes
) {

    public CapabilityDescriptor {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        if (id.isBlank()) {
            throw new IllegalArgumentException("id cannot be blank");
        }

        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static CapabilityDescriptor of(String id, String description, CapabilityType type) {
        return new CapabilityDescriptor(id, description, type, Map.of());
    }

    public static CapabilityDescriptor of(
            String id,
            String description,
            CapabilityType type,
            Map<String, Object> attributes) {
        return new CapabilityDescriptor(id, description, type, attributes);
    }
}
