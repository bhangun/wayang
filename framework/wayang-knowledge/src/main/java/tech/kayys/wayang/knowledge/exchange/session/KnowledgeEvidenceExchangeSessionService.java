package tech.kayys.wayang.knowledge.exchange.session;

import java.time.Duration;
import java.time.Instant;

public interface KnowledgeEvidenceExchangeSessionService {

    KnowledgeEvidenceExchangeSession createSession(
            String localRuntimeId,
            String remoteRuntimeId,
            String principalId,
            String tenantId,
            String workspaceId,
            String projectId,
            Duration lifetime,
            Instant now
    );

    KnowledgeEvidenceExchangeSession establish(
            String sessionId,
            String remoteNonce,
            Instant now
    );

    KnowledgeEvidenceExchangeSession requireEstablished(
            String sessionId,
            Instant now
    );

    void close(
            String sessionId,
            Instant now
    );
}
