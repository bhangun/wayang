package tech.kayys.wayang.execution.governance;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PolicyDecisionTest {

    @Test
    void allow_is_allowed() {
        PolicyDecision d = PolicyDecision.allow();
        assertTrue(d.isAllowed());
        assertInstanceOf(PolicyDecision.Allow.class, d);
    }

    @Test
    void deny_is_not_allowed() {
        PolicyDecision d = PolicyDecision.deny("too dangerous", "my-policy");
        assertFalse(d.isAllowed());
        PolicyDecision.Deny deny = assertInstanceOf(PolicyDecision.Deny.class, d);
        assertEquals("too dangerous", deny.reason());
        assertEquals("my-policy", deny.policyId());
    }

    @Test
    void require_approval_is_not_allowed() {
        PolicyDecision d = PolicyDecision.requireApproval("needs approval", "gate");
        assertFalse(d.isAllowed());
        assertInstanceOf(PolicyDecision.RequireApproval.class, d);
    }
}
