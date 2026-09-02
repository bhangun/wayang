package tech.kayys.wayang.knowledge.exchange.envelope;

import java.time.Instant;
import java.util.Map;

public record KnowledgeEvidenceExchangeKeyReference(
        String keyId,
        String keyVersion,
        KnowledgeEvidenceExchangeMessageAuthenticationAlgorithm algorithm,
        String runtimeId,
        Instant createdAt,
        Instant expiresAt,
        boolean revoked,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeKeyReference {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean usableAt(Instant now) {
        if (revoked) {
            return false;
        }
        if (expiresAt != null && now.isAfter(expiresAt)) {
            return false;
        }
        return true;
    }
}
