package tech.kayys.wayang.knowledge.exchange.envelope;

public record KnowledgeEvidenceExchangeSignedMessage<T>(
        T payload,
        KnowledgeEvidenceExchangeSignedEnvelope envelope
) {
}
