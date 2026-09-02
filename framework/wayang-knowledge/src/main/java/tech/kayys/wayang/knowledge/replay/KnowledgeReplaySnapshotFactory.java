package tech.kayys.wayang.knowledge.replay;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.util.List;
import java.util.Map;

public final class KnowledgeReplaySnapshotFactory {

    private KnowledgeReplaySnapshotFactory() {}

    public static KnowledgeReplaySnapshot fromTrace(KnowledgeDecisionTrace trace) {
        String evidenceFingerprint = KnowledgeFingerprint.sha256(String.join("|", trace.evidenceIds()));
        String policyFingerprint = KnowledgeFingerprint.sha256(String.join("|", trace.policyIds()));
        String configurationFingerprint = KnowledgeFingerprint.sha256(trace.operation() + "|" + trace.agentId());

        return new KnowledgeReplaySnapshot(
                trace.id(),
                trace.executionId(),
                trace.agentId(),
                trace.operation(),
                trace.query(),
                trace.createdAt(),
                trace.evidenceIds(),
                List.of(),
                trace.lineageIds(),
                trace.policyIds(),
                trace.ruleIds(),
                KnowledgeFingerprint.sha256(trace.agentId() + "|" + trace.operation()),
                policyFingerprint,
                evidenceFingerprint,
                configurationFingerprint,
                Map.of()
        );
    }
}
