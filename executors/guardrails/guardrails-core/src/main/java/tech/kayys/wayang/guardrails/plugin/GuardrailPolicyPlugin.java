package tech.kayys.wayang.guardrails.plugin;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.guardrails.policy.PolicyResult;
import tech.kayys.wayang.guardrails.detector.CheckPhase;
import tech.kayys.wayang.guardrails.NodeContext;
import tech.kayys.wayang.plugin.WayangPlugin;

/**
 * Interface for guardrail policy plugins.
 * These plugins define rules that determine whether certain actions should be
 * allowed or denied.
 */
public interface GuardrailPolicyPlugin extends WayangPlugin {

    /**
     * Evaluate the policy against the given context.
     * 
     * @param context The node execution context
     * @return A PolicyResult indicating whether the policy passes or fails
     */
    Uni<PolicyResult> evaluate(NodeContext context);

    /**
     * Get the category of this policy (e.g., "rate_limit", "access_control",
     * "content_policy").
     */
    String getCategory();

    /**
     * Get the phases when this policy should be applied.
     */
    CheckPhase[] applicablePhases();
}