package tech.kayys.wayang.knowledge.snapshot;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.util.Optional;

public interface KnowledgeDecisionSnapshotService {

    KnowledgeDecisionSnapshot capture(
            KnowledgeDecisionTrace trace,
            KnowledgeSnapshotCaptureContext context);

    Optional<KnowledgeDecisionSnapshot> get(String snapshotId);

    Optional<KnowledgeDecisionSnapshot> forTrace(String traceId);
}
