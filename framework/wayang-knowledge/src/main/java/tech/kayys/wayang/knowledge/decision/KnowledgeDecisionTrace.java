package tech.kayys.wayang.knowledge.decision;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Immutable audit representation of a knowledge-driven decision.
 */
public record KnowledgeDecisionTrace(
        String id,
        String executionId,
        String agentId,
        String operation,
        String query,
        List<String> evidenceIds,
        List<String> lineageIds,
        List<String> policyIds,
        List<String> ruleIds,
        List<KnowledgeDecisionStep> steps,
        KnowledgeDecisionOutcome outcome,
        Instant createdAt,
        Map<String, Object> metadata
) {

    public KnowledgeDecisionTrace {
        evidenceIds = evidenceIds == null ? List.of() : List.copyOf(evidenceIds);
        lineageIds = lineageIds == null ? List.of() : List.copyOf(lineageIds);
        policyIds = policyIds == null ? List.of() : List.copyOf(policyIds);
        ruleIds = ruleIds == null ? List.of() : List.copyOf(ruleIds);
        steps = steps == null ? List.of() : List.copyOf(steps);
        createdAt = createdAt == null ? Instant.now() : createdAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean completed() {
        return outcome != null && outcome.status() == KnowledgeDecisionStatus.COMPLETED;
    }

    public boolean allowed() {
        return outcome != null && outcome.status() == KnowledgeDecisionStatus.ALLOWED;
    }

    public boolean denied() {
        return outcome != null && outcome.status() == KnowledgeDecisionStatus.DENIED;
    }

    public boolean ambiguous() {
        return outcome != null && outcome.status() == KnowledgeDecisionStatus.AMBIGUOUS;
    }
}
