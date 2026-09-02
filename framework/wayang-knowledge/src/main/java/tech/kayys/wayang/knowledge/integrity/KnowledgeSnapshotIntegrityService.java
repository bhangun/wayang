package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

public interface KnowledgeSnapshotIntegrityService {

    KnowledgeSnapshotIntegrityResult verify(KnowledgeSnapshotId snapshotId);
}
