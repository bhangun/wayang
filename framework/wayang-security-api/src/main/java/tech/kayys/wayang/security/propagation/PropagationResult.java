package tech.kayys.wayang.security.propagation;

import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.delegation.DelegationContext;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of context propagation containing the attenuated context.
 */
public record PropagationResult(
        SecurityContext context,
        Optional<DelegationContext> delegation
) {

    public PropagationResult {
        Objects.requireNonNull(context, "context");
        delegation = delegation == null ? Optional.empty() : delegation;
    }

    public static PropagationResult of(SecurityContext context) {
        return new PropagationResult(context, Optional.empty());
    }

    public static PropagationResult of(SecurityContext context, DelegationContext delegation) {
        return new PropagationResult(context, Optional.ofNullable(delegation));
    }
}
