package tech.kayys.wayang.knowledge.exchange.session;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeEvidenceExchangeHandshake(
        String handshakeId,
        String localRuntimeId,
        String remoteRuntimeId,
        String principalId,
        String tenantId,
        String workspaceId,
        String projectId,
        String nonce,
        Instant createdAt,
        Instant expiresAt,
        Map<String, String> capabilities,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeHandshake {
        Objects.requireNonNull(handshakeId, "handshakeId");
        Objects.requireNonNull(localRuntimeId, "localRuntimeId");
        Objects.requireNonNull(remoteRuntimeId, "remoteRuntimeId");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(createdAt, "createdAt");
        Objects.requireNonNull(expiresAt, "expiresAt");

        capabilities = capabilities == null ? Map.of() : Map.copyOf(capabilities);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
