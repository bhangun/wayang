package tech.kayys.wayang.security.delegation;

import java.util.List;

/**
 * Attenuation constraints that restrict the authority passed down the delegation chain.
 */
public record DelegationConstraints(
        List<String> allowedCapabilities,
        List<String> allowedActions,
        int maxHops
) {

    public DelegationConstraints {
        allowedCapabilities = allowedCapabilities == null ? List.of("*") : List.copyOf(allowedCapabilities);
        allowedActions      = allowedActions      == null ? List.of("*") : List.copyOf(allowedActions);
    }

    public static DelegationConstraints unrestricted() {
        return new DelegationConstraints(List.of("*"), List.of("*"), 5);
    }

    public static DelegationConstraints empty() {
        return new DelegationConstraints(List.of(), List.of(), 0);
    }

    public static DelegationConstraints of(List<String> capabilities, int maxHops) {
        return new DelegationConstraints(capabilities, List.of("*"), maxHops);
    }

    public boolean permitsCapability(String capability) {
        return allowedCapabilities.contains("*") || allowedCapabilities.contains(capability);
    }

    public DelegationConstraints decrementHop() {
        return new DelegationConstraints(allowedCapabilities, allowedActions, Math.max(0, maxHops - 1));
    }
}
