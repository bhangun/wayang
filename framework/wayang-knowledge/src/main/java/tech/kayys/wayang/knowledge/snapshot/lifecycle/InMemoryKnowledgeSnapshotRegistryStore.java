package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeSnapshotRegistryStore implements KnowledgeSnapshotRegistryStore {

    private final Map<String, KnowledgeDecisionSnapshot> snapshots = new ConcurrentHashMap<>();
    private final Map<String, KnowledgeSnapshotReference> references = new ConcurrentHashMap<>();
    private final Map<String, KnowledgeSnapshotHold> holds = new ConcurrentHashMap<>();
    private final Map<String, KnowledgeSnapshotLifecycleState> states = new ConcurrentHashMap<>();

    @Override
    public void saveSnapshot(KnowledgeDecisionSnapshot snapshot) {
        snapshots.putIfAbsent(snapshot.snapshotId().value(), snapshot);
        states.putIfAbsent(snapshot.snapshotId().value(), KnowledgeSnapshotLifecycleState.ACTIVE);
    }

    @Override
    public Optional<KnowledgeDecisionSnapshot> getSnapshot(KnowledgeSnapshotId snapshotId) {
        return Optional.ofNullable(snapshots.get(snapshotId.value()));
    }

    @Override
    public void saveReference(KnowledgeSnapshotReference reference) {
        references.put(reference.referenceId(), reference);
    }

    @Override
    public void deleteReference(String referenceId) {
        references.remove(referenceId);
    }

    @Override
    public List<KnowledgeSnapshotReference> findReferences(KnowledgeSnapshotId snapshotId) {
        return references.values().stream()
                .filter(ref -> ref.snapshotId().equals(snapshotId))
                .toList();
    }

    @Override
    public void saveHold(KnowledgeSnapshotHold hold) {
        holds.put(hold.holdId(), hold);
    }

    @Override
    public void deleteHold(String holdId) {
        holds.remove(holdId);
    }

    @Override
    public List<KnowledgeSnapshotHold> findHolds(KnowledgeSnapshotId snapshotId) {
        return holds.values().stream()
                .filter(hold -> hold.snapshotId().equals(snapshotId))
                .toList();
    }

    @Override
    public void updateState(KnowledgeSnapshotId snapshotId, KnowledgeSnapshotLifecycleState state) {
        states.put(snapshotId.value(), state);
    }

    @Override
    public List<KnowledgeDecisionSnapshot> findCandidates(Instant before) {
        return new ArrayList<>(snapshots.values());
    }
}
