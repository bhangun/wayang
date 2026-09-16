package tech.kayys.wayang.security.delegation;

import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Default {@link DelegationService} implementation.
 */
public final class DefaultDelegationService implements DelegationService {

    private final DelegationAttenuator attenuator;
    private final DelegationPolicy policy;
    private final Clock clock;

    public DefaultDelegationService(
            DelegationAttenuator attenuator,
            DelegationPolicy policy,
            Clock clock) {
        this.attenuator = Objects.requireNonNull(attenuator, "attenuator");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CompletionStage<DelegationContext> delegate(
            SecurityContextSnapshot parent,
            DelegationRequest request) {

        Instant now = clock.instant();

        if (!canDelegate(parent, request)) {
            return CompletableFuture.failedFuture(
                    new SecurityException("Delegation denied by policy")
            );
        }

        DelegationConstraints parentConstraints = parent.delegation()
                .map(DelegationContext::constraints)
                .orElseGet(DelegationConstraints::unrestricted);

        DelegationConstraints childConstraints = attenuator.attenuate(
                parentConstraints, request.constraints()
        );

        Instant expiresAt = now.plus(request.lifetime());

        // Ensure child does not outlive parent delegation
        if (parent.delegation().isPresent()) {
            DelegationContext parentDelegation = parent.delegation().get();
            if (expiresAt.isAfter(parentDelegation.expiresAt())) {
                return CompletableFuture.failedFuture(
                        new SecurityException("Child delegation cannot outlive parent delegation")
                );
            }
        }

        DelegationContext delegation = new DelegationContext(
                DelegationId.generate(),
                request.audience(),
                childConstraints,
                now,
                expiresAt,
                parent.delegation().map(DelegationContext::id)
        );

        return CompletableFuture.completedFuture(delegation);
    }

    @Override
    public boolean canDelegate(SecurityContextSnapshot parent, DelegationRequest request) {
        return policy.mayDelegate(parent, request);
    }
}
