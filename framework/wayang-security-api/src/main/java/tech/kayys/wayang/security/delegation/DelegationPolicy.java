package tech.kayys.wayang.security.delegation;

import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

/**
 * Policy gate that determines whether a delegation request is permitted.
 *
 * <p>Pluggable — implementations may consult OPA, Keycloak policies,
 * tenant-specific rules, or any other policy engine.
 */
public interface DelegationPolicy {

    /**
     * Returns {@code true} if the principal in {@code parent} is allowed to
     * create a child delegation matching the given {@code request}.
     */
    boolean mayDelegate(
            SecurityContextSnapshot parent,
            DelegationRequest request
    );
}
