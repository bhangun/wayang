package tech.kayys.wayang.knowledge.policy;

import java.util.Map;

/**
 * Result of evaluating a decision policy.
 */
public record PolicyDecision(
        DecisionType type,
        String policyId,
        String reason,
        Map<String, Object> metadata
) {

    public PolicyDecision {
        type = type == null ? DecisionType.ALLOW : type;
        reason = reason == null ? "" : reason;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public enum DecisionType {
        ALLOW,
        DENY,
        REQUIRE_REVIEW,
        REQUIRE_EVIDENCE
    }

    public static PolicyDecision allow(String policyId) {
        return new PolicyDecision(DecisionType.ALLOW, policyId, "", Map.of());
    }

    public static PolicyDecision deny(String policyId, String reason) {
        return new PolicyDecision(DecisionType.DENY, policyId, reason, Map.of());
    }

    public static PolicyDecision requireReview(String policyId, String reason) {
        return new PolicyDecision(DecisionType.REQUIRE_REVIEW, policyId, reason, Map.of());
    }

    public static PolicyDecision requireEvidence(String policyId, String reason) {
        return new PolicyDecision(DecisionType.REQUIRE_EVIDENCE, policyId, reason, Map.of());
    }
}
