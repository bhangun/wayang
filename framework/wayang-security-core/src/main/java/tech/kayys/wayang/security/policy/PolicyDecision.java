package tech.kayys.wayang.security.policy;

import java.util.List;
import java.util.Map;

public record PolicyDecision(
        PolicyEffect effect,
        String policyId,
        String reason,
        List<Obligation> obligations,
        Map<String, Object> attributes
) {

    public PolicyDecision {
        obligations = obligations == null ? List.of() : List.copyOf(obligations);
        attributes  = attributes  == null ? Map.of()  : Map.copyOf(attributes);
    }

    public boolean allowed() { return effect == PolicyEffect.ALLOW; }
    public boolean denied()  { return effect == PolicyEffect.DENY;  }

    public static PolicyDecision allow(String policyId, String reason) {
        return new PolicyDecision(PolicyEffect.ALLOW, policyId, reason, List.of(), Map.of());
    }

    public static PolicyDecision allow(String policyId, String reason, List<Obligation> obligations) {
        return new PolicyDecision(PolicyEffect.ALLOW, policyId, reason, obligations, Map.of());
    }

    public static PolicyDecision deny(String policyId, String reason) {
        return new PolicyDecision(PolicyEffect.DENY, policyId, reason, List.of(), Map.of());
    }
}
