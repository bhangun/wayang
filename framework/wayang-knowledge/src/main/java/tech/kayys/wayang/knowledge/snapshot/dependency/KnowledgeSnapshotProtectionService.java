package tech.kayys.wayang.knowledge.snapshot.dependency;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.List;

public interface KnowledgeSnapshotProtectionService {

    boolean isProtected(KnowledgeSnapshotId snapshotId);

    List<KnowledgeSnapshotDependency> protectionReasons(KnowledgeSnapshotId snapshotId);
}
