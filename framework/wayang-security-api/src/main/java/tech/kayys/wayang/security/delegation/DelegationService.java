package tech.kayys.wayang.security.delegation;

import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

import java.util.concurrent.CompletionStage;

/**
 * Central service for creating attenuated child delegations.
 *
 * <p>This is the only supported mechanism for deriving child authority from
 * an existing security context. Direct construction of {@link DelegationContext}
 * outside this service is discouraged.
 */
public interface DelegationService {

    /**
     * Creates a new child delegation derived from the parent security context.
     *
     * @param parent  the parent security context
     * @param request the delegation parameters
     * @return a future that resolves to the new child DelegationContext
     * @throws SecurityException if delegation is denied
     */
    CompletionStage<DelegationContext> delegate(
            SecurityContextSnapshot parent,
            DelegationRequest request
    );

    /**
     * Returns whether the parent security context may create the requested delegation.
     * Synchronous check — suitable for pre-flight validation.
     */
    boolean canDelegate(
            SecurityContextSnapshot parent,
            DelegationRequest request
    );
}
