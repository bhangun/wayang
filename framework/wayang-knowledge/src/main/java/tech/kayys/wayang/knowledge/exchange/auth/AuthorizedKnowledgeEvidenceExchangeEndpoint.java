package tech.kayys.wayang.knowledge.exchange.auth;

import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeEndpoint;
import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeRequest;
import tech.kayys.wayang.knowledge.exchange.KnowledgeEvidenceExchangeResponse;

import java.time.Instant;
import java.util.Map;

public final class AuthorizedKnowledgeEvidenceExchangeEndpoint
        implements KnowledgeEvidenceExchangeEndpoint {

    private final KnowledgeEvidenceExchangeAuthorizer authorizer;
    private final KnowledgeEvidenceExchangeEndpoint delegate;

    public AuthorizedKnowledgeEvidenceExchangeEndpoint(
            KnowledgeEvidenceExchangeAuthorizer authorizer,
            KnowledgeEvidenceExchangeEndpoint delegate
    ) {
        this.authorizer = authorizer;
        this.delegate = delegate;
    }

    public KnowledgeEvidenceExchangeResponse exchange(
            KnowledgeEvidenceExchangeRequest request,
            KnowledgeEvidenceExchangePrincipal principal
    ) {
        KnowledgeEvidenceExchangeAuthorizationContext context =
                new KnowledgeEvidenceExchangeAuthorizationContext(
                        request,
                        principal,
                        Instant.now(),
                        Map.of()
                );

        KnowledgeEvidenceExchangeAuthorizationDecision decision =
                authorizer.authorize(context);

        if (decision instanceof KnowledgeEvidenceExchangeAuthorizationDecision.Deny deny) {
            return new KnowledgeEvidenceExchangeResponse(
                    false,
                    request.operation(),
                    request.artifactId(),
                    new byte[0],
                    null,
                    null,
                    null,
                    "ACCESS_DENIED",
                    deny.reason(),
                    Map.of("policyId", deny.policyId())
            );
        }

        return delegate.exchange(request);
    }

    @Override
    public KnowledgeEvidenceExchangeResponse exchange(
            KnowledgeEvidenceExchangeRequest request
    ) {
        // Evaluate default authorization or delegate
        return delegate.exchange(request);
    }
}
