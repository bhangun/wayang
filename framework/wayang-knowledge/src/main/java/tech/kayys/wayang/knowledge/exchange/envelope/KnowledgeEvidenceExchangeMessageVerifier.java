package tech.kayys.wayang.knowledge.exchange.envelope;

import tech.kayys.wayang.knowledge.exchange.auth.KnowledgeEvidenceExchangePrincipal;

import java.time.Instant;

public interface KnowledgeEvidenceExchangeMessageVerifier {

    KnowledgeEvidenceExchangeMessageAuthenticationStatus verify(
            KnowledgeEvidenceExchangeSignedEnvelope envelope,
            KnowledgeEvidenceExchangePrincipal principal,
            String expectedRuntimeId,
            String expectedSessionId,
            String expectedRequestId,
            String expectedNonce,
            Instant now
    );

    default KnowledgeEvidenceExchangeMessageAuthenticationStatus verify(
            KnowledgeEvidenceExchangeSignedMessage<?> message,
            String expectedRuntimeId,
            String expectedTenantId,
            Instant now
    ) {
        if (message == null || message.envelope() == null) {
            return KnowledgeEvidenceExchangeMessageAuthenticationStatus.INVALID_MESSAGE;
        }
        var env = message.envelope();
        var principal = (expectedTenantId != null || expectedRuntimeId != null)
                ? KnowledgeEvidenceExchangePrincipal.of("anonymous", expectedRuntimeId, expectedTenantId)
                : null;
        return verify(env, principal, expectedRuntimeId, env.sessionId(), env.requestId(), env.nonce(), now != null ? now : Instant.now());
    }
}
