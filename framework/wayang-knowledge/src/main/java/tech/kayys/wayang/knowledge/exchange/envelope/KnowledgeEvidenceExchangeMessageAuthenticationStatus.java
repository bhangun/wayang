package tech.kayys.wayang.knowledge.exchange.envelope;

public enum KnowledgeEvidenceExchangeMessageAuthenticationStatus {
    AUTHENTICATED,
    INVALID_SIGNATURE,
    INVALID_MAC,
    UNKNOWN_KEY,
    REVOKED_KEY,
    EXPIRED_KEY,
    UNSUPPORTED_ALGORITHM,
    INVALID_MESSAGE,
    INVALID_BINDING
}
