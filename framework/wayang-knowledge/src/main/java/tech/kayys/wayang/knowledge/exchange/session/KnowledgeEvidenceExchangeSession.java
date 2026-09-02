package tech.kayys.wayang.knowledge.exchange.session;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeEvidenceExchangeSession(
        String sessionId,
        String localRuntimeId,
        String remoteRuntimeId,
        String principalId,
        String tenantId,
        String workspaceId,
        String projectId,
        Instant createdAt,
        Instant expiresAt,
        KnowledgeEvidenceExchangeSessionStatus status,
        String localNonce,
        String remoteNonce,
        String sessionFingerprint,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeSession {
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(localRuntimeId, "localRuntimeId");
        Objects.requireNonNull(remoteRuntimeId, "remoteRuntimeId");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(localNonce, "localNonce");
        Objects.requireNonNull(sessionFingerprint, "sessionFingerprint");

        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now");
        return now.isAfter(expiresAt);
    }

    public boolean isActive(Instant now) {
        return status == KnowledgeEvidenceExchangeSessionStatus.ESTABLISHED && !isExpired(now);
    }
}
