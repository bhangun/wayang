package tech.kayys.wayang.knowledge.decision;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class KnowledgeDecisionSteps {

    private KnowledgeDecisionSteps() {}

    public static KnowledgeDecisionStep governance(
            String label,
            List<String> evidenceIds,
            KnowledgeDecisionStatus status,
            String reason) {

        return new KnowledgeDecisionStep(
                UUID.randomUUID().toString(),
                KnowledgeDecisionStepType.GOVERNANCE,
                label,
                evidenceIds,
                List.of(),
                List.of(),
                status,
                reason,
                Instant.now(),
                Map.of()
        );
    }

    public static KnowledgeDecisionStep policy(
            String policyId,
            KnowledgeDecisionStatus status,
            String reason) {

        return new KnowledgeDecisionStep(
                UUID.randomUUID().toString(),
                KnowledgeDecisionStepType.POLICY,
                policyId,
                List.of(),
                List.of(policyId),
                List.of(),
                status,
                reason,
                Instant.now(),
                Map.of()
        );
    }

    public static KnowledgeDecisionStep evidence(
            List<String> evidenceIds,
            String reason) {

        return new KnowledgeDecisionStep(
                UUID.randomUUID().toString(),
                KnowledgeDecisionStepType.EVIDENCE_SELECTION,
                "evidence-selection",
                evidenceIds,
                List.of(),
                List.of(),
                KnowledgeDecisionStatus.ALLOWED,
                reason,
                Instant.now(),
                Map.of()
        );
    }

    public static KnowledgeDecisionStep conflict(
            List<String> evidenceIds,
            KnowledgeDecisionStatus status,
            String reason) {

        return new KnowledgeDecisionStep(
                UUID.randomUUID().toString(),
                KnowledgeDecisionStepType.CONFLICT_RESOLUTION,
                "conflict-resolution",
                evidenceIds,
                List.of(),
                List.of(),
                status,
                reason,
                Instant.now(),
                Map.of()
        );
    }

    public static KnowledgeDecisionStep decision(
            List<String> evidenceIds,
            List<String> ruleIds,
            KnowledgeDecisionStatus status,
            String reason) {

        return new KnowledgeDecisionStep(
                UUID.randomUUID().toString(),
                KnowledgeDecisionStepType.DECISION,
                "decision",
                evidenceIds,
                List.of(),
                ruleIds,
                status,
                reason,
                Instant.now(),
                Map.of()
        );
    }
}
