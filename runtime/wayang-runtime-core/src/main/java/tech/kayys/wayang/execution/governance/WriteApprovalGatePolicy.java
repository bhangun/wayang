package tech.kayys.wayang.execution.governance;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * Built-in policy: WRITE-level tool calls require approval unless the caller
 * holds the "write-auto-approved" role or is in standalone mode (§31).
 *
 * <p>Priority 20 — runs after capability check.</p>
 */
@ApplicationScoped
public class WriteApprovalGatePolicy implements ToolPolicy {

    @Override
    public String id() { return "write-approval-gate"; }

    @Override
    public String description() { return "Requires approval for WRITE-level tool calls in non-standalone deployments."; }

    @Override
    public int priority() { return 20; }

    @Override
    public PolicyDecision evaluate(ToolInvocation invocation, ToolPermissionContext context) {
        ToolCapabilityLevel level = context.level();

        // READ-level and below are always auto-approved
        if (level == ToolCapabilityLevel.READ) {
            return PolicyDecision.allow();
        }

        // Standalone or explicitly auto-approved roles bypass the gate
        if (context.hasRole("*") || context.hasRole("write-auto-approved")) {
            return PolicyDecision.allow();
        }

        // WRITE and above require approval in multi-tenant or authenticated deployments
        if (level.isAtLeast(ToolCapabilityLevel.WRITE)) {
            return PolicyDecision.requireApproval(
                "Tool [" + invocation.name() + "] requires write capability — human approval needed.",
                id()
            );
        }

        return PolicyDecision.allow();
    }
}
