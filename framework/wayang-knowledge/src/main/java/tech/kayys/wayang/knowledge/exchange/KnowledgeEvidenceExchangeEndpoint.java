package tech.kayys.wayang.knowledge.exchange;

public interface KnowledgeEvidenceExchangeEndpoint {
    KnowledgeEvidenceExchangeResponse exchange(
            KnowledgeEvidenceExchangeRequest request
    );
}
