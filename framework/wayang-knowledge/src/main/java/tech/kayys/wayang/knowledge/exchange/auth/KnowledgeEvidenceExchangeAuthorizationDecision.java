package tech.kayys.wayang.knowledge.exchange.auth;

import java.util.Map;

public sealed interface KnowledgeEvidenceExchangeAuthorizationDecision
        permits KnowledgeEvidenceExchangeAuthorizationDecision.Allow,
                KnowledgeEvidenceExchangeAuthorizationDecision.Deny {

    record Allow(
            String policyId,
            Map<String, String> metadata
    ) implements KnowledgeEvidenceExchangeAuthorizationDecision {
        public Allow {
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }

    record Deny(
            String policyId,
            String reason,
            Map<String, String> metadata
    ) implements KnowledgeEvidenceExchangeAuthorizationDecision {
        public Deny {
            metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        }
    }
}
