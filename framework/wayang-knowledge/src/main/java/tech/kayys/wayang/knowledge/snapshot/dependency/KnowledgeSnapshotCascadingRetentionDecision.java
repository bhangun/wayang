package tech.kayys.wayang.knowledge.snapshot.dependency;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;
import tech.kayys.wayang.knowledge.snapshot.lifecycle.KnowledgeSnapshotDeletionDecision;

import java.util.List;
import java.util.Set;

public record KnowledgeSnapshotCascadingRetentionDecision(
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotDeletionDecision decision,
        Set<String> reachableSnapshots,
        List<KnowledgeSnapshotDependency> protectedDependencies,
        List<String> reasons
) {

    public KnowledgeSnapshotCascadingRetentionDecision {
        reachableSnapshots = Set.copyOf(reachableSnapshots);
        protectedDependencies = List.copyOf(protectedDependencies);
        reasons = List.copyOf(reasons);
    }
}
