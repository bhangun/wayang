package tech.kayys.wayang.knowledge.exchange.session;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeEvidenceExchangeReplayRecord(
        String requestId,
        String sessionId,
        String nonce,
        String bindingFingerprint,
        String principalId,
        String tenantId,
        String runtimeId,
        Instant firstSeenAt,
        Instant expiresAt,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeReplayRecord {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(bindingFingerprint, "bindingFingerprint");
        Objects.requireNonNull(firstSeenAt, "firstSeenAt");
        Objects.requireNonNull(expiresAt, "expiresAt");

        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean expired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
