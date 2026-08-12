package tech.kayys.wayang.execution;

import java.util.List;
import java.util.Map;

/**
 * SPI for guardrail checks on tool invocations.
 *
 * <p>Implementations are discovered via CDI and are <em>optional</em>.
 * When no implementation is present in the deployment, tool execution proceeds
 * without guardrail validation.
 *
 * <p>This interface is deliberately <strong>synchronous/blocking</strong>. The
 * reactive {@code GuardrailsService} (Mutiny-based) should be adapted to this
 * interface in the {@code wayang-guardrails-runtime} module, keeping
 * {@code wayang-runtime-core} free of any Mutiny/Quarkus-reactive dependency.
 *
 * <p>Typical pipeline position (inside {@code DefaultAgentToolExecutor}):
 * <pre>
 *   existence check
 *     → schema validation
 *       → approval policy
 *         → <strong>guardrail check  ← this interface</strong>
 *           → circuit breaker
 *             → retry
 *               → timeout
 *                 → Tool.execute()
 * </pre>
 */
public interface ToolGuardrailCheck {

    /**
     * Checks whether the tool call is permitted by active guardrail policies.
     *
     * @param toolName  The name of the tool about to be executed.
     * @param arguments The resolved arguments map for this invocation.
     * @return A {@link Result} indicating whether the call is allowed and,
     *         if blocked, the reason.
     */
    Result check(String toolName, Map<String, Object> arguments);

    // -------------------------------------------------------------------------

    /**
     * Outcome of a guardrail check.
     *
     * @param allowed          {@code true} if the tool may proceed.
     * @param reason           Human-readable reason for a block (null if allowed).
     * @param triggeredPolicies IDs of the policies that triggered the block.
     */
    record Result(boolean allowed, String reason, List<String> triggeredPolicies) {

        public static Result allow() {
            return new Result(true, null, List.of());
        }

        public static Result block(String reason, List<String> policies) {
            return new Result(false, reason, policies);
        }

        public static Result block(String reason) {
            return new Result(false, reason, List.of());
        }
    }
}
