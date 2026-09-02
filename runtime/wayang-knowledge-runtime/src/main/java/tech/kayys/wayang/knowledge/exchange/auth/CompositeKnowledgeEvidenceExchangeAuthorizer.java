package tech.kayys.wayang.knowledge.exchange.auth;

import java.util.List;
import java.util.Map;

public final class CompositeKnowledgeEvidenceExchangeAuthorizer
        implements KnowledgeEvidenceExchangeAuthorizer {

    private final List<KnowledgeEvidenceExchangeAuthorizer> policies;

    public CompositeKnowledgeEvidenceExchangeAuthorizer(
            List<KnowledgeEvidenceExchangeAuthorizer> policies
    ) {
        this.policies = policies == null ? List.of() : List.copyOf(policies);
    }

    @Override
    public KnowledgeEvidenceExchangeAuthorizationDecision authorize(
            KnowledgeEvidenceExchangeAuthorizationContext context
    ) {
        for (KnowledgeEvidenceExchangeAuthorizer policy : policies) {
            KnowledgeEvidenceExchangeAuthorizationDecision decision = policy.authorize(context);
            if (decision instanceof KnowledgeEvidenceExchangeAuthorizationDecision.Deny) {
                return decision;
            }
        }

        return new KnowledgeEvidenceExchangeAuthorizationDecision.Allow(
                "composite",
                Map.of()
        );
    }
}
