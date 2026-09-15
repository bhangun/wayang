package tech.kayys.wayang.security.enforcement;

import java.util.concurrent.CompletionStage;

/**
 * Entry point for security enforcement (PEP).
 * Coordinates policy authorization decisions, obligation execution, and data transformation.
 */
public interface PolicyEnforcementPoint {

    CompletionStage<EnforcementResult> enforce(EnforcementRequest request);
}
