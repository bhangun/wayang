package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.util.List;

public record KnowledgeSnapshotLifecycleDecision(
        KnowledgeSnapshotId snapshotId,
        KnowledgeSnapshotDeletionDecision decision,
        KnowledgeSnapshotLifecycleState currentState,
        List<KnowledgeSnapshotReference> activeReferences,
        List<KnowledgeSnapshotHold> activeHolds,
        List<String> reasons
) {

    public KnowledgeSnapshotLifecycleDecision {
        activeReferences = activeReferences == null ? List.of() : List.copyOf(activeReferences);
        activeHolds = activeHolds == null ? List.of() : List.copyOf(activeHolds);
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }
}
