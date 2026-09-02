package tech.kayys.wayang.knowledge.exchange.envelope;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DefaultKnowledgeEvidenceExchangeMessageSigner
        implements KnowledgeEvidenceExchangeMessageSigner {

    private final KnowledgeEvidenceExchangeMessageAuthenticator authenticator;
    private final KnowledgeEvidenceExchangeMessageCanonicalizer canonicalizer;

    public DefaultKnowledgeEvidenceExchangeMessageSigner(
            KnowledgeEvidenceExchangeMessageAuthenticator authenticator,
            KnowledgeEvidenceExchangeMessageCanonicalizer canonicalizer
    ) {
        this.authenticator = Objects.requireNonNull(authenticator, "authenticator");
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer");
    }

    @Override
    public KnowledgeEvidenceExchangeSignedEnvelope sign(
            String requestId,
            String sessionId,
            String nonce,
            String runtimeId,
            String remoteRuntimeId,
            String tenantId,
            String workspaceId,
            String projectId,
            String messageType,
            String messageFingerprint,
            Instant issuedAt,
            Instant expiresAt,
            Instant now
    ) {
        String envelopeId = UUID.randomUUID().toString();

        KnowledgeEvidenceExchangeSignedEnvelope provisional = new KnowledgeEvidenceExchangeSignedEnvelope(
                envelopeId,
                requestId,
                sessionId,
                nonce,
                runtimeId,
                remoteRuntimeId,
                tenantId,
                workspaceId,
                projectId,
                messageType,
                messageFingerprint,
                authenticator.algorithm(),
                authenticator.keyId(),
                authenticator.keyVersion(),
                new byte[0],
                issuedAt,
                expiresAt,
                Map.of()
        );

        byte[] canonical = canonicalizer.canonicalize(provisional);
        byte[] authentication = authenticator.authenticate(canonical);

        return new KnowledgeEvidenceExchangeSignedEnvelope(
                envelopeId,
                requestId,
                sessionId,
                nonce,
                runtimeId,
                remoteRuntimeId,
                tenantId,
                workspaceId,
                projectId,
                messageType,
                messageFingerprint,
                authenticator.algorithm(),
                authenticator.keyId(),
                authenticator.keyVersion(),
                authentication,
                issuedAt,
                expiresAt,
                Map.of()
        );
    }
}
