package tech.kayys.wayang.knowledge.exchange.envelope;

import tech.kayys.wayang.knowledge.exchange.auth.KnowledgeEvidenceExchangePrincipal;

import java.time.Instant;
import java.util.Objects;

public final class DefaultKnowledgeEvidenceExchangeMessageVerifier
        implements KnowledgeEvidenceExchangeMessageVerifier {

    private final KnowledgeEvidenceExchangeKeyResolver keyResolver;
    private final KnowledgeEvidenceExchangeMessageCanonicalizer canonicalizer;

    public DefaultKnowledgeEvidenceExchangeMessageVerifier(
            KnowledgeEvidenceExchangeKeyResolver keyResolver,
            KnowledgeEvidenceExchangeMessageCanonicalizer canonicalizer
    ) {
        this.keyResolver = Objects.requireNonNull(keyResolver, "keyResolver");
        this.canonicalizer = Objects.requireNonNull(canonicalizer, "canonicalizer");
    }

    @Override
    public KnowledgeEvidenceExchangeMessageAuthenticationStatus verify(
            KnowledgeEvidenceExchangeSignedEnvelope envelope,
            KnowledgeEvidenceExchangePrincipal principal,
            String expectedRuntimeId,
            String expectedSessionId,
            String expectedRequestId,
            String expectedNonce,
            Instant now
    ) {
        if (envelope.expired(now)) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_MESSAGE;
        }

        if (expectedRuntimeId != null && !expectedRuntimeId.equals(envelope.runtimeId())) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_BINDING;
        }

        if (expectedSessionId != null && !expectedSessionId.equals(envelope.sessionId())) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_BINDING;
        }

        if (expectedRequestId != null && !expectedRequestId.equals(envelope.requestId())) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_BINDING;
        }

        if (expectedNonce != null && !expectedNonce.equals(envelope.nonce())) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_BINDING;
        }

        if (principal != null && principal.tenantId() != null && envelope.tenantId() != null
                && !principal.tenantId().equals(envelope.tenantId())) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_BINDING;
        }

        var authenticatorOpt = keyResolver.resolveVerifier(
                envelope.keyId(),
                envelope.keyVersion(),
                now
        );

        if (authenticatorOpt.isEmpty()) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.UNKNOWN_KEY;
        }

        KnowledgeEvidenceExchangeMessageAuthenticator authenticator = authenticatorOpt.get();

        if (authenticator.algorithm() != envelope.authenticationAlgorithm()) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.UNSUPPORTED_ALGORITHM;
        }

        KnowledgeEvidenceExchangeSignedEnvelope provisional = new KnowledgeEvidenceExchangeSignedEnvelope(
                envelope.envelopeId(),
                envelope.requestId(),
                envelope.sessionId(),
                envelope.nonce(),
                envelope.runtimeId(),
                envelope.remoteRuntimeId(),
                envelope.tenantId(),
                envelope.workspaceId(),
                envelope.projectId(),
                envelope.messageType(),
                envelope.messageFingerprint(),
                envelope.authenticationAlgorithm(),
                envelope.keyId(),
                envelope.keyVersion(),
                new byte[0],
                envelope.issuedAt(),
                envelope.expiresAt(),
                envelope.metadata()
        );

        byte[] canonical = canonicalizer.canonicalize(provisional);
        boolean valid = authenticator.verify(canonical, envelope.authentication());

        return valid
                ? KnowledgeEvidenceExchangeMessageAuthenticationStatus.AUTHENTICATED
                : KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_SIGNATURE;
    }
}
