package tech.kayys.wayang.knowledge.exchange.auth;

public interface KnowledgeEvidenceExchangeAuthorizer {
    KnowledgeEvidenceExchangeAuthorizationDecision authorize(
            KnowledgeEvidenceExchangeAuthorizationContext context
    );
}
