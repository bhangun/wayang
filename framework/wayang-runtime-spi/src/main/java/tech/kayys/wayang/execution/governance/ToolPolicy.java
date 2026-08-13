package tech.kayys.wayang.execution.governance;

import tech.kayys.wayang.tool.ToolInvocation;

/**
 * SPI for a single governance policy evaluated before a tool executes (§30).
 *
 * <p>Policies are ordered and composable. Each policy is given the full invocation
 * and permission context; it returns a {@link PolicyDecision} which the
 * {@link ToolPolicyEvaluator} aggregates.</p>
 *
 * <p>Implementations can be CDI beans or registered via {@code ServiceLoader}.</p>
 */
public interface ToolPolicy {

    /**
     * Unique stable ID for this policy (e.g. "capability-level-check", "shell-deny").
     * Used in audit records and {@link PolicyDecision} payloads.
     */
    String id();

    /**
     * Optional human-readable description for audit UIs.
     */
    default String description() { return id(); }

    /**
     * Priority order — lower value = evaluated first.
     * Default is 100 (mid-tier). DENY policies should be high-priority (low number).
     */
    default int priority() { return 100; }

    /**
     * Evaluate the policy for the given invocation and context.
     *
     * @param invocation The tool call being evaluated.
     * @param context    The caller's permission context.
     * @return A {@link PolicyDecision}; never null.
     */
    PolicyDecision evaluate(ToolInvocation invocation, ToolPermissionContext context);
}
