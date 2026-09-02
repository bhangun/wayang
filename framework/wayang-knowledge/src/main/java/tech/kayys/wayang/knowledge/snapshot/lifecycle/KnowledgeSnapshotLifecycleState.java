package tech.kayys.wayang.knowledge.snapshot.lifecycle;

public enum KnowledgeSnapshotLifecycleState {
    ACTIVE,
    RETAINED,
    ARCHIVED,
    DELETE_PENDING,
    DELETED,
    BLOCKED
}
