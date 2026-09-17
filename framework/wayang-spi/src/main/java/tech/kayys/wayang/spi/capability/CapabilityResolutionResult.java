package tech.kayys.wayang.spi.capability;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Result of resolving a set of capability requirements against available providers.
 */
public record CapabilityResolutionResult(
        Map<String, Capability> resolved,
        Set<String> missing,
        boolean satisfied
) {

    public CapabilityResolutionResult {
        resolved = resolved == null ? Map.of() : Map.copyOf(resolved);
        missing = missing == null ? Set.of() : Set.copyOf(missing);
    }

    public static CapabilityResolutionResult success(Map<String, Capability> resolved) {
        return new CapabilityResolutionResult(resolved, Set.of(), true);
    }

    public static CapabilityResolutionResult partial(
            Map<String, Capability> resolved,
            Set<String> missing,
            boolean satisfied) {
        return new CapabilityResolutionResult(resolved, missing, satisfied);
    }

    public static CapabilityResolutionResult failure(Set<String> missing) {
        return new CapabilityResolutionResult(Map.of(), missing, false);
    }
}
