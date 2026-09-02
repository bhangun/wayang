package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DefaultKnowledgeSnapshotRegistry implements KnowledgeSnapshotRegistry {

    private final KnowledgeSnapshotRegistryStore store;
    private final KnowledgeSnapshotRetentionPolicy policy;

    public DefaultKnowledgeSnapshotRegistry(
            KnowledgeSnapshotRegistryStore store,
            KnowledgeSnapshotRetentionPolicy policy) {

        this.store = Objects.requireNonNull(store, "store is required");
        this.policy = policy != null ? policy : KnowledgeSnapshotRetentionPolicy.defaults();
    }

    @Override
    public void register(KnowledgeDecisionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot is required");
        store.saveSnapshot(snapshot);
    }

    @Override
    public Optional<KnowledgeDecisionSnapshot> get(KnowledgeSnapshotId snapshotId) {
        return store.getSnapshot(snapshotId);
    }

    @Override
    public void addReference(KnowledgeSnapshotReference reference) {
        store.saveReference(reference);
    }

    @Override
    public void removeReference(String referenceId) {
        store.deleteReference(referenceId);
    }

    @Override
    public List<KnowledgeSnapshotReference> references(KnowledgeSnapshotId snapshotId) {
        return store.findReferences(snapshotId);
    }

    @Override
    public void addHold(KnowledgeSnapshotHold hold) {
        store.saveHold(hold);
    }

    @Override
    public void removeHold(String holdId) {
        store.deleteHold(holdId);
    }

    @Override
    public List<KnowledgeSnapshotHold> holds(KnowledgeSnapshotId snapshotId) {
        return store.findHolds(snapshotId);
    }

    @Override
    public KnowledgeSnapshotLifecycleDecision evaluate(KnowledgeSnapshotId snapshotId, Instant now) {
        KnowledgeDecisionSnapshot snapshot = store.getSnapshot(snapshotId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown snapshot: " + snapshotId.value()));

        List<KnowledgeSnapshotReference> activeReferences = store.findReferences(snapshotId).stream()
                .filter(ref -> ref.isActive(now))
                .toList();

        List<KnowledgeSnapshotHold> activeHolds = store.findHolds(snapshotId).stream()
                .filter(hold -> hold.activeAt(now))
                .toList();

        if (!activeHolds.isEmpty()) {
            return new KnowledgeSnapshotLifecycleDecision(
                    snapshotId,
                    KnowledgeSnapshotDeletionDecision.BLOCKED,
                    KnowledgeSnapshotLifecycleState.BLOCKED,
                    activeReferences,
                    activeHolds,
                    List.of("Active retention hold")
            );
        }

        if (!activeReferences.isEmpty() && policy.requireNoReferences()) {
            return new KnowledgeSnapshotLifecycleDecision(
                    snapshotId,
                    KnowledgeSnapshotDeletionDecision.RETAIN,
                    KnowledgeSnapshotLifecycleState.RETAINED,
                    activeReferences,
                    activeHolds,
                    List.of("Active snapshot references")
            );
        }

        return new KnowledgeSnapshotLifecycleDecision(
                snapshotId,
                KnowledgeSnapshotDeletionDecision.DELETE,
                KnowledgeSnapshotLifecycleState.DELETE_PENDING,
                activeReferences,
                activeHolds,
                List.of("No active references or holds")
        );
    }
}
