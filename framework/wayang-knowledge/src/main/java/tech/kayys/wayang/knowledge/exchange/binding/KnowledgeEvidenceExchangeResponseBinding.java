package tech.kayys.wayang.knowledge.exchange.binding;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeOperation;
import tech.kayys.wayang.knowledge.snapshot.artifact.KnowledgeEvidenceArtifactId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record KnowledgeEvidenceExchangeResponseBinding(
        String requestId,
        String sessionId,
        String requestNonce,
        String responseId,
        String runtimeId,
        String remoteRuntimeId,
        String tenantId,
        String workspaceId,
        String projectId,
        KnowledgeEvidenceExchangeOperation operation,
        KnowledgeEvidenceArtifactId artifactId,
        String resourceId,
        boolean success,
        String contentFingerprint,
        String manifestFingerprint,
        String merkleProofFingerprint,
        Instant issuedAt,
        Instant expiresAt,
        String responseFingerprint,
        Map<String, String> metadata
) {
    public KnowledgeEvidenceExchangeResponseBinding {
        Objects.requireNonNull(requestId, "requestId");
        Objects.requireNonNull(sessionId, "sessionId");
        Objects.requireNonNull(requestNonce, "requestNonce");
        Objects.requireNonNull(responseId, "responseId");
        Objects.requireNonNull(runtimeId, "runtimeId");
        Objects.requireNonNull(operation, "operation");
        Objects.requireNonNull(issuedAt, "issuedAt");
        Objects.requireNonNull(expiresAt, "expiresAt");
        Objects.requireNonNull(responseFingerprint, "responseFingerprint");

        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }
}
