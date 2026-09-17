package tech.kayys.wayang.spi.capability;

import java.util.List;
import java.util.Objects;

/**
 * Collection of capability requirements.
 */
public record CapabilityRequirements(
        List<CapabilityRequirement> requirements
) {

    public CapabilityRequirements {
        requirements = requirements == null ? List.of() : List.copyOf(requirements);
    }

    public static CapabilityRequirements of(CapabilityRequirement... requirements) {
        return new CapabilityRequirements(List.of(requirements));
    }

    public static CapabilityRequirements of(List<CapabilityRequirement> requirements) {
        return new CapabilityRequirements(requirements);
    }

    public static CapabilityRequirements empty() {
        return new CapabilityRequirements(List.of());
    }

    public boolean isEmpty() {
        return requirements.isEmpty();
    }
}
