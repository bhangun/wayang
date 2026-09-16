package tech.kayys.wayang.api.rest.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import tech.kayys.wayang.security.delegation.DefaultDelegationAttenuator;
import tech.kayys.wayang.security.delegation.DefaultDelegationService;
import tech.kayys.wayang.security.delegation.DelegationAttenuator;
import tech.kayys.wayang.security.delegation.DelegationPolicy;
import tech.kayys.wayang.security.delegation.DelegationService;

import java.time.Clock;

/**
 * CDI Producer providing security delegation services.
 */
@ApplicationScoped
public class SecurityProducer {

    @Produces
    @ApplicationScoped
    public DelegationAttenuator produceAttenuator() {
        return new DefaultDelegationAttenuator();
    }

    @Produces
    @ApplicationScoped
    public DelegationPolicy producePolicy() {
        // Default policy permits valid authenticated delegations
        return (parent, request) -> true;
    }

    @Produces
    @ApplicationScoped
    public DelegationService produceDelegationService(
            DelegationAttenuator attenuator,
            DelegationPolicy policy) {
        return new DefaultDelegationService(attenuator, policy, Clock.systemUTC());
    }
}
