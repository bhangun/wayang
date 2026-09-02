package tech.kayys.wayang.knowledge.exchange.envelope;

import tech.kayys.wayang.knowledge.exchange.binding.KnowledgeEvidenceExchangeResponseEnvelope;

public record KnowledgeEvidenceExchangeAuthenticatedResponse(
        KnowledgeEvidenceExchangeResponseEnvelope response,
        KnowledgeEvidenceExchangeSignedEnvelope authentication
) {
}
