package tech.kayys.wayang.execution.governance;

import tech.kayys.wayang.tool.ToolInvocation;
import java.util.List;

/**
 * Evaluates an ordered list of {@link ToolPolicy} instances against an invocation
 * and returns the aggregate {@link PolicyDecision} (§30 — Policy Engine).
 *
 * <p>Evaluation rules:
 * <ol>
 *   <li>Policies are sorted by {@link ToolPolicy#priority()} ascending (lower = first).</li>
 *   <li>The first {@link PolicyDecision.Deny} short-circuits — no further policies run.</li>
 *   <li>If any policy returns {@link PolicyDecision.RequireApproval}, that is the result
 *       (after all non-deny policies complete).</li>
 *   <li>If all policies return {@link PolicyDecision.Allow}, the tool is permitted.</li>
 * </ol>
 */
public interface ToolPolicyEvaluator {

    /**
     * Evaluate all registered policies for the given invocation and context.
     *
     * @param invocation The tool invocation to check.
     * @param context    The caller's permission context.
     * @return Aggregate decision; never null.
     */
    PolicyDecision evaluate(ToolInvocation invocation, ToolPermissionContext context);

    /**
     * Returns all registered policies in evaluation order.
     */
    List<ToolPolicy> policies();
}
