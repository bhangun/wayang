package tech.kayys.wayang.knowledge.snapshot.pack;

public enum KnowledgeSnapshotPackageVerificationStatus {
    VERIFIED,
    INVALID_MANIFEST,
    SNAPSHOT_NOT_FOUND,
    INTEGRITY_FAILED,
    SEAL_FAILED,
    DEPENDENCY_MISSING,
    RESOURCE_MISSING,
    FINGERPRINT_MISMATCH,
    GOVERNANCE_MISMATCH,
    POLICY_MISMATCH,
    RULE_MISMATCH,
    INCOMPLETE,
    FAILED
}
