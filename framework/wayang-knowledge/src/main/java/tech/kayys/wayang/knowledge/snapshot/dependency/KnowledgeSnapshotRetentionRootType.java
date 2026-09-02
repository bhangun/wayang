package tech.kayys.wayang.knowledge.snapshot.dependency;

public enum KnowledgeSnapshotRetentionRootType {
    ACTIVE_EXECUTION,
    DECISION_TRACE,
    AUDIT_EVENT,
    REPLAY,
    DERIVATION,
    LEGAL_HOLD,
    MANUAL_HOLD,
    EXTERNAL_REFERENCE
}
