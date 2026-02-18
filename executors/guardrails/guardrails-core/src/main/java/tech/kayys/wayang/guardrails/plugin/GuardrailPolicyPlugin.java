package tech.kayys.wayang.guardrails.plugin;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.guardrails.policy.PolicyCheckResult;
import tech.kayys.wayang.plugin.WayangPlugin;

/**
 * Interface for guardrail policy plugins.
 * These plugins define rules that determine whether certain actions should be allowed or denied.
 */
public interface GuardrailPolicyPlugin extends WayangPlugin {
    
    /**
     * Evaluate the policy against the given context.
     * 
     * @param context The node execution context
     * @return A PolicyCheckResult indicating whether the policy passes or fails
     */
    Uni<PolicyCheckResult> evaluate(NodeContext context);
    
    /**
     * Get the category of this policy (e.g., "rate_limit", "access_control", "content_policy").
     */
    String getCategory();
    
    /**
     * Get whether this policy applies to pre-execution, post-execution, or both.
     */
    GuardrailDetectorPlugin.CheckPhase[] applicablePhases();
}