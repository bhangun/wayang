package tech.kayys.wayang.knowledge.snapshot.dependency;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.List;
import java.util.Objects;

public final class DefaultKnowledgeSnapshotProtectionService implements KnowledgeSnapshotProtectionService {

    private final KnowledgeSnapshotDependencyGraph graph;

    public DefaultKnowledgeSnapshotProtectionService(KnowledgeSnapshotDependencyGraph graph) {
        this.graph = Objects.requireNonNull(graph);
    }

    @Override
    public boolean isProtected(KnowledgeSnapshotId snapshotId) {
        return !graph.dependents(snapshotId.value()).isEmpty();
    }

    @Override
    public List<KnowledgeSnapshotDependency> protectionReasons(KnowledgeSnapshotId snapshotId) {
        return graph.dependents(snapshotId.value());
    }
}
