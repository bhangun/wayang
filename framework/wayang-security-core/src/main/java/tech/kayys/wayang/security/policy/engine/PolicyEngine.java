package tech.kayys.wayang.security.policy.engine;

import tech.kayys.wayang.security.authz.AuthorizationRequest;
import tech.kayys.wayang.security.policy.PolicyDecision;

/**
 * Evaluates an {@link AuthorizationRequest} against registered policies and returns a {@link PolicyDecision}.
 */
public interface PolicyEngine {

    PolicyDecision evaluate(AuthorizationRequest request);
}
