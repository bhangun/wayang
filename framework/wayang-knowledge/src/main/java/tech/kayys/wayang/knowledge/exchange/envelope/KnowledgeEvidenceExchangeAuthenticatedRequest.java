package tech.kayys.wayang.knowledge.exchange.envelope;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeRequest;
import tech.kayys.wayang.knowledge.exchange.session.KnowledgeEvidenceExchangeRequestBinding;

public record KnowledgeEvidenceExchangeAuthenticatedRequest(
        KnowledgeEvidenceExchangeRequest request,
        KnowledgeEvidenceExchangeRequestBinding binding,
        KnowledgeEvidenceExchangeSignedEnvelope authentication
) {
}
