package tech.kayys.wayang.knowledge.decision;

import java.util.Map;

/**
 * Final structured outcome of a governed knowledge decision.
 */
public record KnowledgeDecisionOutcome(
        KnowledgeDecisionStatus status,
        String code,
        String summary,
        double confidence,
        Map<String, Object> metadata
) {

    public KnowledgeDecisionOutcome {
        confidence = Math.max(0.0, Math.min(1.0, confidence));
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeDecisionOutcome allowed(String code, String summary) {
        return new KnowledgeDecisionOutcome(KnowledgeDecisionStatus.ALLOWED, code, summary, 1.0, Map.of());
    }

    public static KnowledgeDecisionOutcome denied(String code, String summary) {
        return new KnowledgeDecisionOutcome(KnowledgeDecisionStatus.DENIED, code, summary, 1.0, Map.of());
    }

    public static KnowledgeDecisionOutcome ambiguous(String code, String summary) {
        return new KnowledgeDecisionOutcome(KnowledgeDecisionStatus.AMBIGUOUS, code, summary, 0.0, Map.of());
    }
}
