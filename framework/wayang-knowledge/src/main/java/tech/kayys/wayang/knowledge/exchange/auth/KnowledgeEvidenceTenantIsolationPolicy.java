package tech.kayys.wayang.knowledge.exchange.auth;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeRequest;

import java.util.Map;

public final class KnowledgeEvidenceTenantIsolationPolicy
        implements KnowledgeEvidenceExchangeAuthorizer {

    @Override
    public KnowledgeEvidenceExchangeAuthorizationDecision authorize(
            KnowledgeEvidenceExchangeAuthorizationContext context
    ) {
        KnowledgeEvidenceExchangePrincipal principal = context.principal();
        KnowledgeEvidenceExchangeRequest request = context.request();

        String requestedTenant = request.metadata().get("tenantId");

        if (requestedTenant == null) {
            return new KnowledgeEvidenceExchangeAuthorizationDecision.Deny(
                    "tenant-isolation",
                    "Tenant context is required",
                    Map.of()
            );
        }

        if (principal == null || principal.tenantId() == null || !requestedTenant.equals(principal.tenantId())) {
            return new KnowledgeEvidenceExchangeAuthorizationDecision.Deny(
                    "tenant-isolation",
                    "Cross-tenant evidence access denied",
                    Map.of(
                            "principalTenant", principal != null ? String.valueOf(principal.tenantId()) : "null",
                            "requestedTenant", requestedTenant
                    )
            );
        }

        return new KnowledgeEvidenceExchangeAuthorizationDecision.Allow(
                "tenant-isolation",
                Map.of()
        );
    }
}
