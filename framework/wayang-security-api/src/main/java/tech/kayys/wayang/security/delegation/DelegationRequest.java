package tech.kayys.wayang.security.delegation;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * A request to create a child delegation from an existing security context.
 *
 * <p>The lifetime must be positive and, when validated by the DelegationService,
 * must not exceed the remaining lifetime of the parent delegation.
 */
public record DelegationRequest(
        DelegationAudience audience,
        DelegationConstraints constraints,
        Duration lifetime,
        Map<String, Object> attributes
) {
    public DelegationRequest {
        Objects.requireNonNull(audience, "audience must not be null");
        constraints = constraints == null ? DelegationConstraints.empty() : constraints;

        if (lifetime == null || lifetime.isNegative() || lifetime.isZero()) {
            throw new IllegalArgumentException("lifetime must be positive");
        }
        attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
    }

    public static DelegationRequest of(
            DelegationAudience audience,
            DelegationConstraints constraints,
            Duration lifetime) {
        return new DelegationRequest(audience, constraints, lifetime, Map.of());
    }
}
