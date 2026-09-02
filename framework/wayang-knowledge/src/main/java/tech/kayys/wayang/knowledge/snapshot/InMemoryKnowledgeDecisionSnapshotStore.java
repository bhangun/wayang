package tech.kayys.wayang.knowledge.snapshot;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeDecisionSnapshotStore implements KnowledgeDecisionSnapshotStore {

    private final Map<String, KnowledgeDecisionSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public void save(KnowledgeDecisionSnapshot snapshot) {
        if (snapshot == null || snapshot.snapshotId() == null) {
            throw new IllegalArgumentException("snapshot and snapshotId are required");
        }

        String id = snapshot.snapshotId().value();
        KnowledgeDecisionSnapshot existing = snapshots.putIfAbsent(id, snapshot);
        if (existing != null && !existing.equals(snapshot)) {
            throw new IllegalStateException("Snapshot is immutable: " + id);
        }
    }

    @Override
    public Optional<KnowledgeDecisionSnapshot> get(String snapshotId) {
        return Optional.ofNullable(snapshots.get(snapshotId));
    }

    @Override
    public Optional<KnowledgeDecisionSnapshot> getByTrace(String traceId) {
        return snapshots.values().stream()
                .filter(s -> traceId != null && traceId.equals(s.traceId()))
                .findFirst();
    }

    @Override
    public List<KnowledgeDecisionSnapshot> findByExecution(String executionId) {
        return snapshots.values().stream()
                .filter(s -> executionId != null && executionId.equals(s.executionId()))
                .toList();
    }
}
