package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

public interface KnowledgeSnapshotArchive {

    void archive(KnowledgeDecisionSnapshot snapshot);

    boolean contains(KnowledgeSnapshotId snapshotId);

    void restore(KnowledgeSnapshotId snapshotId);
}
