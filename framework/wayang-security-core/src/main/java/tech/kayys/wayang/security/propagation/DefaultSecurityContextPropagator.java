package tech.kayys.wayang.security.propagation;

import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.delegation.DelegationAudience;
import tech.kayys.wayang.security.delegation.DelegationConstraints;
import tech.kayys.wayang.security.delegation.DelegationContext;
import tech.kayys.wayang.security.delegation.DelegationId;
import tech.kayys.wayang.security.identity.Principal;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultSecurityContextPropagator implements SecurityContextPropagator {

    private final long defaultTtlSeconds;

    public DefaultSecurityContextPropagator(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public DefaultSecurityContextPropagator() {
        this(3600); // 1 hour default
    }

    @Override
    public PropagationResult propagate(PropagationRequest request) {
        Objects.requireNonNull(request, "request");

        DelegationConstraints constraints = request.constraints() != null
                ? request.constraints()
                : DelegationConstraints.unrestricted();

        DelegationAudience audience = DelegationAudience.of(request.targetAgentId());

        Instant now = Instant.now();
        DelegationContext delegation = new DelegationContext(
                DelegationId.generate(),
                audience,
                constraints.decrementHop(),
                now,
                now.plusSeconds(defaultTtlSeconds),
                Optional.empty()
        );

        Map<String, Object> propagatedAttributes = new HashMap<>(request.source().attributes());
        propagatedAttributes.put("delegated_by", request.source().principal().id());
        propagatedAttributes.put("delegated_to", request.targetAgentId());
        propagatedAttributes.put("remaining_hops", delegation.constraints().maxHops());

        SecurityContext attenuatedContext = new SecurityContext(
                request.source().principal(),
                request.source().tenant(),
                propagatedAttributes
        );

        return PropagationResult.of(attenuatedContext, delegation);
    }
}
