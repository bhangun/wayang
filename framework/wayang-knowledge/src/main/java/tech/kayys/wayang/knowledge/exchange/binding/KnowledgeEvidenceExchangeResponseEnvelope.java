package tech.kayys.wayang.knowledge.exchange.binding;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeResponse;

public record KnowledgeEvidenceExchangeResponseEnvelope(
        KnowledgeEvidenceExchangeResponse response,
        KnowledgeEvidenceExchangeResponseBinding binding
) {
}
