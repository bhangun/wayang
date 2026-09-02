package tech.kayys.wayang.knowledge.exchange.session;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeOperation;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeEvidenceExchangeRequestBinding(
        String requestId,
        String sessionId,
        String nonce,
        String runtimeId,
        String remoteRuntimeId,
        String tenantId,
        String workspaceId,
        String projectId,
        KnowledgeEvidenceExchangeOperation operation,
        String artifactId,
        String resourceId,
        Instant issuedAt,
        Instant expiresAt,
        String correlationId,
        String bindingFingerprint,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeRequestBinding {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(nonce, "nonce");
        Objects.requireNonNull(runtimeId, "runtimeId");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(bindingFingerprint, "bindingFingerprint");

        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
