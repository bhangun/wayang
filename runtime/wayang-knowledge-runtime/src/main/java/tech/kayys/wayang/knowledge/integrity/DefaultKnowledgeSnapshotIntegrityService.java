package tech.kayys.wayang.knowledge.integrity;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;
import tech.kayys.wayang.knowledge.snapshot.KnowledgeSnapshotId;
import tech.kayys.wayang.knowledge.snapshot.lifecycle.KnowledgeSnapshotRegistry;

import java.util.Objects;

public final class DefaultKnowledgeSnapshotIntegrityService implements KnowledgeSnapshotIntegrityService {

    private final KnowledgeSnapshotRegistry registry;
    private final KnowledgeSnapshotIntegrityVerifier verifier;

    public DefaultKnowledgeSnapshotIntegrityService(
            KnowledgeSnapshotRegistry registry,
            KnowledgeSnapshotIntegrityVerifier verifier) {

        this.registry = Objects.requireNonNull(registry, "registry is required");
        this.verifier = Objects.requireNonNull(verifier, "verifier is required");
    }

    @Override
    public KnowledgeSnapshotIntegrityResult verify(KnowledgeSnapshotId snapshotId) {
        KnowledgeDecisionSnapshot snapshot = registry.get(snapshotId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown snapshot: " + snapshotId.value()));
        return verifier.verify(snapshot);
    }
}
