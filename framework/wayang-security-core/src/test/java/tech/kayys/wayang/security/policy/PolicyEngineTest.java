package tech.kayys.wayang.security.policy;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.security.authz.AuthorizationRequest;
import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.obligation.Obligation;
import tech.kayys.wayang.security.obligation.StandardObligations;
import tech.kayys.wayang.security.policy.engine.DefaultPolicyEngine;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyEngineTest {

    @Test
    void testAllowMatchingRule() {
        PolicyRule rule = PolicyRule.allow("rule-1", "user-123", "coding.*");
        Policy policy = Policy.of("policy-1", List.of(rule));

        DefaultPolicyEngine engine = new DefaultPolicyEngine(List.of(policy));

        Principal principal = new Principal("user-123", "Alice", tech.kayys.wayang.security.identity.IdentityType.USER, java.util.Map.of());
        SecurityContext context = SecurityContext.of(principal, TenantContext.of("tenant-1"));

        AuthorizationRequest request = AuthorizationRequest.execute(context, "coding.review");

        PolicyDecision decision = engine.evaluate(request);
        assertTrue(decision.allowed());
        assertEquals("rule-1", decision.policyId());
    }

    @Test
    void testDenyByDefaultWhenNoMatch() {
        PolicyRule rule = PolicyRule.allow("rule-1", "user-123", "coding.review");
        Policy policy = Policy.of("policy-1", List.of(rule));

        DefaultPolicyEngine engine = new DefaultPolicyEngine(List.of(policy));

        Principal principal = new Principal("user-456", "Bob", tech.kayys.wayang.security.identity.IdentityType.USER, java.util.Map.of());
        SecurityContext context = SecurityContext.of(principal, TenantContext.of("tenant-1"));

        AuthorizationRequest request = AuthorizationRequest.execute(context, "coding.deploy");

        PolicyDecision decision = engine.evaluate(request);
        assertFalse(decision.allowed());
        assertEquals("default-deny", decision.policyId());
    }

    @Test
    void testObligationsAttachedToDecision() {
        PolicyRule rule = new PolicyRule(
                "audit-rule",
                "*",
                "finance.*",
                "*",
                PolicyEffect.ALLOW,
                List.of(Obligation.before(StandardObligations.AUDIT), Obligation.before(StandardObligations.HITL)),
                10
        );
        Policy policy = Policy.of("finance-policy", List.of(rule));

        DefaultPolicyEngine engine = new DefaultPolicyEngine(List.of(policy));

        Principal principal = new Principal("agent-x", "Agent X", tech.kayys.wayang.security.identity.IdentityType.AGENT, java.util.Map.of());
        SecurityContext context = SecurityContext.of(principal, TenantContext.of("tenant-1"));

        AuthorizationRequest request = AuthorizationRequest.execute(context, "finance.transfer");
        PolicyDecision decision = engine.evaluate(request);

        assertTrue(decision.allowed());
        assertEquals(2, decision.obligations().size());
        assertEquals(StandardObligations.AUDIT, decision.obligations().get(0).type());
        assertEquals(StandardObligations.HITL, decision.obligations().get(1).type());
    }
}
