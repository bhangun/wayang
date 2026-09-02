package tech.kayys.wayang.knowledge.exchange.auth;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeRequest;

import java.time.Instant;
import java.util.Map;

public record KnowledgeEvidenceExchangeAuthorizationContext(
        KnowledgeEvidenceExchangeRequest request,
        KnowledgeEvidenceExchangePrincipal principal,
        Instant timestamp,
        Map<String, String> attributes
) {
    public KnowledgeEvidenceExchangeAuthorizationContext {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
