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
}
