package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

public interface KnowledgeSnapshotDeletionService {

    void delete(KnowledgeSnapshotId snapshotId);
}
