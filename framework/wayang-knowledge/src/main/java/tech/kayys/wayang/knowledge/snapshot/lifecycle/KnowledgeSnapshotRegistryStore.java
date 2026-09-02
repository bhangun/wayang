package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface KnowledgeSnapshotRegistryStore {

    void saveSnapshot(KnowledgeDecisionSnapshot snapshot);

    Optional<KnowledgeDecisionSnapshot> getSnapshot(KnowledgeSnapshotId snapshotId);

    void saveReference(KnowledgeSnapshotReference reference);

    void deleteReference(String referenceId);

    List<KnowledgeSnapshotReference> findReferences(KnowledgeSnapshotId snapshotId);

    void saveHold(KnowledgeSnapshotHold hold);

    void deleteHold(String holdId);

    List<KnowledgeSnapshotHold> findHolds(KnowledgeSnapshotId snapshotId);

    void updateState(KnowledgeSnapshotId snapshotId, KnowledgeSnapshotLifecycleState state);

    List<KnowledgeDecisionSnapshot> findCandidates(Instant before);
}
