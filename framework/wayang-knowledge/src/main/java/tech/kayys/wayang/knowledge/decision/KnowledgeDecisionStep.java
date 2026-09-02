package tech.kayys.wayang.knowledge.decision;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * One auditable step in a knowledge decision.
 */
public record KnowledgeDecisionStep(
        String id,
        KnowledgeDecisionStepType type,
        String label,
        List<String> evidenceIds,
        List<String> policyIds,
        List<String> ruleIds,
        KnowledgeDecisionStatus status,
        String reason,
        Instant createdAt,
        Map<String, Object> metadata
) {

    public KnowledgeDecisionStep {
        evidenceIds = evidenceIds == null ? List.of() : List.copyOf(evidenceIds);
        policyIds = policyIds == null ? List.of() : List.copyOf(policyIds);
        ruleIds = ruleIds == null ? List.of() : List.copyOf(ruleIds);
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeDecisionStep of(
            String id,
            KnowledgeDecisionStepType type,
            String label,
            KnowledgeDecisionStatus status,
            String reason) {

        return new KnowledgeDecisionStep(
                id,
                type,
                label,
                List.of(),
                List.of(),
                List.of(),
                status,
                reason,
                Instant.now(),
                Map.of()
        );
    }
}
