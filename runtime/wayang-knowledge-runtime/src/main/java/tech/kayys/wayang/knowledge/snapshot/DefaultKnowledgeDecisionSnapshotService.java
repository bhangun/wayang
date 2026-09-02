package tech.kayys.wayang.knowledge.snapshot;

import tech.kayys.wayang.knowledge.decision.KnowledgeDecisionTrace;

import java.util.Objects;
import java.util.Optional;

public final class DefaultKnowledgeDecisionSnapshotService implements KnowledgeDecisionSnapshotService {

    private final KnowledgeDecisionSnapshotFactory factory;
    private final KnowledgeDecisionSnapshotStore store;

    public DefaultKnowledgeDecisionSnapshotService(
            KnowledgeDecisionSnapshotFactory factory,
            KnowledgeDecisionSnapshotStore store) {

        this.factory = Objects.requireNonNull(factory, "factory is required");
        this.store = Objects.requireNonNull(store, "store is required");
    }

    @Override
    public KnowledgeDecisionSnapshot capture(
            KnowledgeDecisionTrace trace,
            KnowledgeSnapshotCaptureContext context) {

        Objects.requireNonNull(trace, "trace is required");
        Objects.requireNonNull(context, "context is required");

        KnowledgeDecisionSnapshot snapshot = factory.create(
                trace,
                context.knowledge(),
                context.policies(),
                context.rules(),
                context.governance(),
                context.runtime()
        );

        store.save(snapshot);
        return snapshot;
    }

    @Override
    public Optional<KnowledgeDecisionSnapshot> get(String snapshotId) {
        return store.get(snapshotId);
    }

    @Override
    public Optional<KnowledgeDecisionSnapshot> forTrace(String traceId) {
        return store.getByTrace(traceId);
    }
}
