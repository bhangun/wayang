package tech.kayys.wayang.knowledge.exchange.auth;

import java.util.Map;

public final class KnowledgeEvidenceScopeIsolationPolicy
        implements KnowledgeEvidenceExchangeAuthorizer {

    @Override
    public KnowledgeEvidenceExchangeAuthorizationDecision authorize(
            KnowledgeEvidenceExchangeAuthorizationContext context
    ) {
        KnowledgeEvidenceExchangePrincipal principal = context.principal();
        Map<String, String> metadata = context.request().metadata();

        if (principal == null) {
            return deny("Principal is missing");
        }

        if (!matches(principal.workspaceId(), metadata.get("workspaceId"))) {
            return deny("Workspace isolation violation");
        }

        if (!matches(principal.projectId(), metadata.get("projectId"))) {
            return deny("Project isolation violation");
        }

        return new KnowledgeEvidenceExchangeAuthorizationDecision.Allow(
                "scope-isolation",
                Map.of()
        );
    }

    private boolean matches(String principalValue, String requestedValue) {
        if (requestedValue == null) {
            return true;
        }
        return requestedValue.equals(principalValue);
    }

    private KnowledgeEvidenceExchangeAuthorizationDecision.Deny deny(String reason) {
        return new KnowledgeEvidenceExchangeAuthorizationDecision.Deny(
                "scope-isolation",
                reason,
                Map.of()
        );
    }
}
