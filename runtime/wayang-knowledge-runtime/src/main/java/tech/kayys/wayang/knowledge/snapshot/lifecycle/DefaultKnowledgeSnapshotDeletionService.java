package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;

import java.time.Instant;
import java.util.Objects;

public final class DefaultKnowledgeSnapshotDeletionService implements KnowledgeSnapshotDeletionService {

    private final KnowledgeSnapshotRegistry registry;
    private final KnowledgeSnapshotRegistryStore store;

    public DefaultKnowledgeSnapshotDeletionService(
            KnowledgeSnapshotRegistry registry,
            KnowledgeSnapshotRegistryStore store) {

        this.registry = Objects.requireNonNull(registry);
        this.store = Objects.requireNonNull(store);
    }

    @Override
    public void delete(KnowledgeSnapshotId snapshotId) {
        KnowledgeSnapshotLifecycleDecision decision = registry.evaluate(snapshotId, Instant.now());

        if (decision.decision() != KnowledgeSnapshotDeletionDecision.DELETE) {
            throw new IllegalStateException("Snapshot cannot be safely deleted: " + snapshotId.value());
        }

        if (!decision.activeReferences().isEmpty()) {
            throw new IllegalStateException("Snapshot still has active references");
        }

        if (!decision.activeHolds().isEmpty()) {
            throw new IllegalStateException("Snapshot is under active hold");
        }

        store.updateState(snapshotId, KnowledgeSnapshotLifecycleState.DELETED);
    }
}
