package tech.kayys.wayang.security.propagation;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.delegation.DelegationConstraints;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SecurityContextPropagatorTest {

    @Test
    void testPropagateAttenuatesContextAndDecrementsHops() {
        DefaultSecurityContextPropagator propagator = new DefaultSecurityContextPropagator(1800);

        Principal principal = Principal.agent("parent-agent", "Parent Agent");
        SecurityContext source = SecurityContext.of(principal, TenantContext.of("tenant-x"));

        DelegationConstraints constraints = new DelegationConstraints(
                List.of("tool.read", "tool.search"),
                List.of("execute"),
                3
        );

        PropagationRequest request = PropagationRequest.of(source, "child-agent", constraints);
        PropagationResult result = propagator.propagate(request);

        assertNotNull(result);
        assertNotNull(result.context());
        assertTrue(result.delegation().isPresent());

        // Check delegation properties
        var delegation = result.delegation().get();
        assertEquals(2, delegation.constraints().maxHops());
        assertTrue(delegation.audience().allows("child-agent"));
        assertFalse(delegation.audience().allows("other-agent"));

        // Check context attributes
        assertEquals("parent-agent", result.context().attributes().get("delegated_by"));
        assertEquals("child-agent", result.context().attributes().get("delegated_to"));
        assertEquals(2, result.context().attributes().get("remaining_hops"));
    }
}
