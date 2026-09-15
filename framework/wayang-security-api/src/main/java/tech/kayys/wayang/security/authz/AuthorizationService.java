package tech.kayys.wayang.security.authz;

import java.util.concurrent.CompletionStage;

/**
 * SPI for authorization.
 * Connects to RBAC, ABAC, tenant-policy, and capability-policy engines
 * without coupling Wayang core to any specific implementation.
 */
public interface AuthorizationService {

    CompletionStage<AuthorizationDecision> authorize(AuthorizationRequest request);
}
