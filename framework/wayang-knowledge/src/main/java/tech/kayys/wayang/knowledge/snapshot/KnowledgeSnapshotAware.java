package tech.kayys.wayang.knowledge.snapshot;

public interface KnowledgeSnapshotAware {

    String snapshotId();

    default boolean hasSnapshot() {
        return snapshotId() != null && !snapshotId().isBlank();
    }
}
