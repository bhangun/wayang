package tech.kayys.wayang.knowledge.exchange.auth;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeOperation;

import java.util.Map;

public final class KnowledgeEvidenceExchangeOperationPolicy
        implements KnowledgeEvidenceExchangeAuthorizer {

    @Override
    public KnowledgeEvidenceExchangeAuthorizationDecision authorize(
            KnowledgeEvidenceExchangeAuthorizationContext context
    ) {
        KnowledgeEvidenceExchangePrincipal principal = context.principal();
        KnowledgeEvidenceExchangeOperation operation = context.request().operation();

        if (principal == null) {
            return new KnowledgeEvidenceExchangeAuthorizationDecision.Deny(
                    "exchange-operation",
                    "Principal is required",
                    Map.of()
            );
        }

        if (principal.roles().contains("evidence-admin")) {
            return new KnowledgeEvidenceExchangeAuthorizationDecision.Allow(
                    "exchange-operation",
                    Map.of()
            );
        }

        if (operation == KnowledgeEvidenceExchangeOperation.VERIFY_ARTIFACT ||
            operation == KnowledgeEvidenceExchangeOperation.VERIFY_RESOURCE ||
            operation == KnowledgeEvidenceExchangeOperation.GET_MERKLE_PROOF ||
            operation == KnowledgeEvidenceExchangeOperation.RESOLVE_ARTIFACT) {
            return new KnowledgeEvidenceExchangeAuthorizationDecision.Allow(
                    "exchange-operation",
                    Map.of()
            );
        }

        if (operation == KnowledgeEvidenceExchangeOperation.GET_RESOURCE ||
            operation == KnowledgeEvidenceExchangeOperation.GET_MANIFEST) {
            if (principal.roles().contains("evidence-reader") || principal.roles().contains("reader") || principal.roles().isEmpty()) {
                return new KnowledgeEvidenceExchangeAuthorizationDecision.Allow(
                        "exchange-operation",
                        Map.of()
                );
            }
            return new KnowledgeEvidenceExchangeAuthorizationDecision.Deny(
                    "exchange-operation",
                    "Evidence reader role required",
                    Map.of()
            );
        }

        return new KnowledgeEvidenceExchangeAuthorizationDecision.Deny(
                "exchange-operation",
                "Operation is not authorized",
                Map.of()
        );
    }
}
