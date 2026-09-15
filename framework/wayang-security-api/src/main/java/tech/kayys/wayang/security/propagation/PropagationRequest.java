package tech.kayys.wayang.security.propagation;

import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.delegation.DelegationConstraints;

import java.util.Objects;

/**
 * Request to propagate security context to a downstream target agent.
 */
public record PropagationRequest(
        SecurityContext source,
        String targetAgentId,
        DelegationConstraints constraints
) {

    public PropagationRequest {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(targetAgentId, "targetAgentId");
        constraints = constraints == null ? DelegationConstraints.unrestricted() : constraints;
    }

    public static PropagationRequest of(SecurityContext source, String targetAgentId) {
        return new PropagationRequest(source, targetAgentId, DelegationConstraints.unrestricted());
    }

    public static PropagationRequest of(SecurityContext source, String targetAgentId, DelegationConstraints constraints) {
        return new PropagationRequest(source, targetAgentId, constraints);
    }
}
