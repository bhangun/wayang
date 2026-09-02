package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;

public interface KnowledgeSnapshotIntegrityVerifier {

    KnowledgeSnapshotIntegrityResult verify(KnowledgeDecisionSnapshot snapshot);
}
