package tech.kayys.wayang.execution.governance;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import tech.kayys.wayang.tool.ToolInvocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

/**
 * Default CDI-managed {@link ToolPolicyEvaluator}.
 *
 * <p>Discovers all {@link ToolPolicy} beans via CDI, sorts them by
 * {@link ToolPolicy#priority()}, and evaluates them in order.
 * Short-circuits on the first {@link PolicyDecision.Deny}.
 * {@link PolicyDecision.RequireApproval} is latched (first one wins if multiple policies fire it).</p>
 */
@ApplicationScoped
public class DefaultToolPolicyEvaluator implements ToolPolicyEvaluator {

    private static final Logger LOG = Logger.getLogger(DefaultToolPolicyEvaluator.class.getName());

    @Inject
    Instance<ToolPolicy> policyInstances;

    @Override
    public PolicyDecision evaluate(ToolInvocation invocation, ToolPermissionContext context) {
        List<ToolPolicy> sorted = policies();

        PolicyDecision.RequireApproval approvalPending = null;

        for (ToolPolicy policy : sorted) {
            PolicyDecision decision;
            try {
                decision = policy.evaluate(invocation, context);
            } catch (Exception e) {
                LOG.warning(() -> "Policy [" + policy.id() + "] threw exception, treating as DENY: " + e.getMessage());
                return PolicyDecision.deny("Policy evaluation error in " + policy.id(), policy.id());
            }

            switch (decision) {
                case PolicyDecision.Deny d -> {
                    LOG.fine(() -> "DENY from policy [" + policy.id() + "]: " + d.reason());
                    return d;
                }
                case PolicyDecision.RequireApproval ra -> {
                    if (approvalPending == null) approvalPending = ra; // latch first
                }
                default -> {} // Allow — continue
            }
        }

        return approvalPending != null ? approvalPending : PolicyDecision.allow();
    }

    @Override
    public List<ToolPolicy> policies() {
        List<ToolPolicy> result = new ArrayList<>();
        if (policyInstances != null) {
            policyInstances.forEach(result::add);
        }
        result.sort(Comparator.comparingInt(ToolPolicy::priority));
        return List.copyOf(result);
    }
}
