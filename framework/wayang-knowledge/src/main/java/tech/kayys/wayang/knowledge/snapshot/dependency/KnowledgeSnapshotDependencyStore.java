package tech.kayys.wayang.knowledge.snapshot.dependency;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.List;

public interface KnowledgeSnapshotDependencyStore {

    void save(KnowledgeSnapshotDependency dependency);

    void delete(String dependencyId);

    List<KnowledgeSnapshotDependency> findBySnapshot(KnowledgeSnapshotId snapshotId);

    List<KnowledgeSnapshotDependency> findByTarget(String targetId);
}
