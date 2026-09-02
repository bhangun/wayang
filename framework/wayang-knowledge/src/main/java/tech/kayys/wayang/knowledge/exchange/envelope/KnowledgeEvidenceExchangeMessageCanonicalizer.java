package tech.kayys.wayang.knowledge.exchange.envelope;

public interface KnowledgeEvidenceExchangeMessageCanonicalizer {
    byte[] canonicalize(KnowledgeEvidenceExchangeSignedEnvelope envelope);
}
