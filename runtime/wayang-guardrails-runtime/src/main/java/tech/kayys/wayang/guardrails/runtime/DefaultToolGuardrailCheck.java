package tech.kayys.wayang.guardrails.runtime;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import tech.kayys.wayang.execution.ToolGuardrailCheck;
import tech.kayys.wayang.guardrails.GuardrailResult;
import tech.kayys.wayang.guardrails.GuardrailsService;

/**
 * CDI implementation of {@link ToolGuardrailCheck} that bridges to the
 * reactive {@link GuardrailsService} using a blocking await.
 *
 * <p>This bean is provided by the {@code wayang-guardrails-runtime} module.
 * When the module is on the classpath, it is discovered automatically by CDI and
 * used by {@code DefaultAgentToolExecutor} as the guardrail gate in the tool
 * execution pipeline.  When the module is absent, the gate is silently skipped.
 *
 * <p>The guardrail check blocks for at most {@link #GUARDRAIL_TIMEOUT} to avoid
 * stalling the agent loop indefinitely on a slow detector.
 */
@ApplicationScoped
public class DefaultToolGuardrailCheck implements ToolGuardrailCheck {

    private static final Logger LOG = Logger.getLogger(DefaultToolGuardrailCheck.class.getName());

    /** Maximum time to wait for the reactive guardrail check to complete. */
    private static final Duration GUARDRAIL_TIMEOUT = Duration.ofSeconds(5);

    @Inject
    GuardrailsService guardrailsService;

    // -------------------------------------------------------------------------
    // ToolGuardrailCheck
    // -------------------------------------------------------------------------

    @Override
    public Result check(String toolName, Map<String, Object> arguments) {
        // Serialize the tool call to a single text blob so the detector can
        // scan it for PII, toxicity, injection attempts, etc.
        String text = buildCheckText(toolName, arguments);

        try {
            GuardrailResult guardResult = guardrailsService
                .preCheck(text, Map.of("toolName", toolName))
                .await()
                .atMost(GUARDRAIL_TIMEOUT);

            if (!guardResult.allowed()) {
                List<String> triggered = guardResult.triggeredPolicies() != null
                    ? guardResult.triggeredPolicies()
                    : List.of();
                String reason = guardResult.reason() != null
                    ? guardResult.reason()
                    : "Guardrail policy blocked tool execution";
                LOG.warning(() -> "Guardrail blocked tool [" + toolName + "]: " + reason
                    + " policies=" + triggered);
                return Result.block(reason, triggered);
            }

            return Result.allow();

        } catch (Exception e) {
            // Fail open — guardrail timeout or error should not halt the agent.
            // Log and allow; a production deployment can flip this to fail-closed.
            LOG.log(Level.WARNING,
                "Guardrail check error for tool [" + toolName + "] — failing open: " + e.getMessage(), e);
            return Result.allow();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a plain-text representation of the tool call for the detector to scan.
     * Format: {@code tool:<name> args:<key>=<value> ...}
     */
    private String buildCheckText(String toolName, Map<String, Object> arguments) {
        StringBuilder sb = new StringBuilder("tool:").append(toolName);
        if (arguments != null && !arguments.isEmpty()) {
            sb.append(" args:");
            arguments.forEach((k, v) -> sb.append(k).append('=').append(v).append(' '));
        }
        return sb.toString().trim();
    }
}
