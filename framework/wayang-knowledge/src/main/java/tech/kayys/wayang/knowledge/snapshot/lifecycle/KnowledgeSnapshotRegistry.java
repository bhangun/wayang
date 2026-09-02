package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface KnowledgeSnapshotRegistry {

    void register(KnowledgeDecisionSnapshot snapshot);

    Optional<KnowledgeDecisionSnapshot> get(KnowledgeSnapshotId snapshotId);

    void addReference(KnowledgeSnapshotReference reference);

    void removeReference(String referenceId);

    List<KnowledgeSnapshotReference> references(KnowledgeSnapshotId snapshotId);

    void addHold(KnowledgeSnapshotHold hold);

    void removeHold(String holdId);

    List<KnowledgeSnapshotHold> holds(KnowledgeSnapshotId snapshotId);

    KnowledgeSnapshotLifecycleDecision evaluate(KnowledgeSnapshotId snapshotId, Instant now);
}
