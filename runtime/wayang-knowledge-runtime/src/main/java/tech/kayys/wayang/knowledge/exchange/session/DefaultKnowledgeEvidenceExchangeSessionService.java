package tech.kayys.wayang.knowledge.exchange.session;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeEvidenceExchangeSessionService
        implements KnowledgeEvidenceExchangeSessionService {

    private final KnowledgeEvidenceExchangeSessionStore store;
    private final SecureRandom secureRandom = new SecureRandom();

    public DefaultKnowledgeEvidenceExchangeSessionService(
            KnowledgeEvidenceExchangeSessionStore store
    ) {
        this.store = Objects.requireNonNull(store, "store");
    }

    @Override
    public KnowledgeEvidenceExchangeSession createSession(
            String localRuntimeId,
            String remoteRuntimeId,
            String principalId,
            String tenantId,
            String workspaceId,
            String projectId,
            Duration lifetime,
            Instant now
    ) {
        String sessionId = UUID.randomUUID().toString();
        String localNonce = generateNonce();
        Instant expiresAt = now.plus(lifetime != null ? lifetime : Duration.ofHours(1));

        String fingerprint = "session:" + sessionId;

        KnowledgeEvidenceExchangeSession session = new KnowledgeEvidenceExchangeSession(
                sessionId,
                localRuntimeId,
                remoteRuntimeId,
                principalId,
                tenantId,
                workspaceId,
                projectId,
                now,
                expiresAt,
                KnowledgeEvidenceExchangeSessionStatus.CREATED,
                localNonce,
                null,
                fingerprint,
                Map.of()
        );

        store.create(session);
        return session;
    }

    @Override
    public KnowledgeEvidenceExchangeSession establish(
            String sessionId,
            String remoteNonce,
            Instant now
    ) {
        KnowledgeEvidenceExchangeSession session = store.get(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (session.isExpired(now)) {
            throw new IllegalStateException("Session expired: " + sessionId);
        }

        KnowledgeEvidenceExchangeSession updated = new KnowledgeEvidenceExchangeSession(
                session.sessionId(),
                session.localRuntimeId(),
                session.remoteRuntimeId(),
                session.principalId(),
                session.tenantId(),
                session.workspaceId(),
                session.projectId(),
                session.createdAt(),
                session.expiresAt(),
                KnowledgeEvidenceExchangeSessionStatus.ESTABLISHED,
                session.localNonce(),
                remoteNonce != null ? remoteNonce : generateNonce(),
                session.sessionFingerprint(),
                session.metadata()
        );

        store.update(updated);
        return updated;
    }

    @Override
    public KnowledgeEvidenceExchangeSession requireEstablished(
            String sessionId,
            Instant now
    ) {
        KnowledgeEvidenceExchangeSession session = store.get(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.isActive(now)) {
            throw new IllegalStateException("Session is not active or established: " + sessionId);
        }

        return session;
    }

    @Override
    public void close(String sessionId, Instant now) {
        store.get(sessionId).ifPresent(session -> {
            KnowledgeEvidenceExchangeSession closed = new KnowledgeEvidenceExchangeSession(
                    session.sessionId(),
                    session.localRuntimeId(),
                    session.remoteRuntimeId(),
                    session.principalId(),
                    session.tenantId(),
                    session.workspaceId(),
                    session.projectId(),
                    session.createdAt(),
                    now,
                    KnowledgeEvidenceExchangeSessionStatus.CLOSED,
                    session.localNonce(),
                    session.remoteNonce(),
                    session.sessionFingerprint(),
                    session.metadata()
            );
            store.update(closed);
        });
    }

    private String generateNonce() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
