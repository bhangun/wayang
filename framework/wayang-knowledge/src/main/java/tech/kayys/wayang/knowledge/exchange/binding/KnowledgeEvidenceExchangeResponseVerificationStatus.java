package tech.kayys.wayang.knowledge.exchange.binding;

public enum KnowledgeEvidenceExchangeResponseVerificationStatus {
    VALID,
    REQUEST_MISMATCH,
    SESSION_MISMATCH,
    NONCE_MISMATCH,
    RUNTIME_MISMATCH,
    TENANT_MISMATCH,
    SCOPE_MISMATCH,
    OPERATION_MISMATCH,
    ARTIFACT_MISMATCH,
    CONTENT_MISMATCH,
    MANIFEST_MISMATCH,
    MERKLE_PROOF_MISMATCH,
    FINGERPRINT_MISMATCH,
    EXPIRED,
    INVALID
}
