package tech.kayys.wayang.knowledge.snapshot.lifecycle;

import tech.kayys.wayang.knowledge.snapshot.KnowledgeDecisionSnapshot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DefaultKnowledgeSnapshotGarbageCollector implements KnowledgeSnapshotGarbageCollector {

    private final KnowledgeSnapshotRegistry registry;
    private final KnowledgeSnapshotRegistryStore store;
    private final KnowledgeSnapshotRetentionPolicy policy;

    public DefaultKnowledgeSnapshotGarbageCollector(
            KnowledgeSnapshotRegistry registry,
            KnowledgeSnapshotRegistryStore store,
            KnowledgeSnapshotRetentionPolicy policy) {

        this.registry = Objects.requireNonNull(registry);
        this.store = Objects.requireNonNull(store);
        this.policy = Objects.requireNonNull(policy);
    }

    @Override
    public List<KnowledgeSnapshotLifecycleDecision> collect(Instant now) {
        if (!policy.allowAutomaticDeletion()) {
            return List.of();
        }

        List<KnowledgeSnapshotLifecycleDecision> decisions = new ArrayList<>();
        for (KnowledgeDecisionSnapshot snapshot : store.findCandidates(now)) {
            decisions.add(registry.evaluate(snapshot.snapshotId(), now));
        }
        return decisions;
    }
}
