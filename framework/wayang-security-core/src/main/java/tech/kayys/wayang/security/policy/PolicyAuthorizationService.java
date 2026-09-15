package tech.kayys.wayang.security.policy;

import tech.kayys.wayang.security.authz.AuthorizationDecision;
import tech.kayys.wayang.security.authz.AuthorizationRequest;
import tech.kayys.wayang.security.authz.AuthorizationService;
import tech.kayys.wayang.security.policy.engine.PolicyEngine;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * {@link AuthorizationService} backed by a {@link PolicyEngine}.
 * Translates a {@link PolicyDecision} into an {@link AuthorizationDecision}.
 */
public final class PolicyAuthorizationService implements AuthorizationService {

    private final PolicyEngine engine;

    public PolicyAuthorizationService(PolicyEngine engine) {
        this.engine = Objects.requireNonNull(engine, "engine");
    }

    @Override
    public CompletionStage<AuthorizationDecision> authorize(AuthorizationRequest request) {
        PolicyDecision decision = engine.evaluate(request);

        // Convert obligations list to a simple map for the AuthorizationDecision
        Map<String, Object> obligations = Map.of("obligations", decision.obligations());

        AuthorizationDecision result = new AuthorizationDecision(
                decision.allowed(),
                decision.reason(),
                obligations
        );

        return CompletableFuture.completedFuture(result);
    }
}
