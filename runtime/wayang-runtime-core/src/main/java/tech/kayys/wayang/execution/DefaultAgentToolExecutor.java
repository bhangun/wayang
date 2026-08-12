package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import tech.kayys.wayang.resilience.CircuitBreakerRegistry;
import tech.kayys.wayang.resilience.DefaultRetryPolicy;
import tech.kayys.wayang.resilience.Retry;
import tech.kayys.wayang.resilience.RetryPolicy;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolResult;

/**
 * Default implementation of {@link AgentToolExecutor}.
 *
 * <p>Implements a real tool execution pipeline:
 * <ol>
 *   <li>Capability / authorization check (tool existence)</li>
 *   <li>Schema validation (required fields in arguments)</li>
 *   <li>Approval policy (writes require HITL in production)</li>
 *   <li>Guardrail check ({@link ToolGuardrailCheck} — optional CDI bean)</li>
 *   <li>Circuit breaker protection</li>
 *   <li>Retry with exponential back-off</li>
 *   <li>Timeout enforcement (30s hard cap per tool call)</li>
 *   <li>{@link Tool#execute(ToolInvocation, Object)} — the actual execution</li>
 * </ol>
 */
@ApplicationScoped
public class DefaultAgentToolExecutor implements AgentToolExecutor, AgentToolExecutor.ToolAware {

    private static final Logger LOG = Logger.getLogger(DefaultAgentToolExecutor.class.getName());

    /** Tools wired via CDI — each Tool bean registers itself automatically. */
    @Inject
    Instance<Tool> toolInstances;

    /**
     * Optional guardrail check — provided by {@code wayang-guardrails-runtime} when deployed.
     * If the bean is not present, the guardrail step is silently skipped.
     */
    @Inject
    Instance<ToolGuardrailCheck> guardrailCheckInstances;

    /**
     * One circuit breaker per tool name — keeps failures isolated.
     * Plain field rather than @Inject so the module stays self-contained.
     */
    private final CircuitBreakerRegistry circuitBreakerRegistry = new CircuitBreakerRegistry();

    /** 3 attempts, 500ms initial delay, 2× back-off, 10s max wait between retries. */
    private static final RetryPolicy RETRY_POLICY =
        new DefaultRetryPolicy(3, 500, 10_000, 2.0, java.util.List.of(Exception.class));

    /** Hard timeout for a single tool execution. */
    private static final long TOOL_TIMEOUT_SECONDS = 30;

    // -------------------------------------------------------------------------
    // AgentToolExecutor.ToolAware
    // -------------------------------------------------------------------------

    @Override
    public java.util.List<Tool> availableTools() {
        java.util.List<Tool> tools = new java.util.ArrayList<>();
        if (toolInstances != null) {
            toolInstances.forEach(tools::add);
        }
        return tools;
    }

    // -------------------------------------------------------------------------
    // AgentToolExecutor
    // -------------------------------------------------------------------------

    @Override
    public CompletionStage<AgentDecision> execute(ToolInvocation invocation) {
        String toolName = invocation.name();

        // --- 1. Capability / existence check ---
        Tool tool = resolveTool(toolName);
        if (tool == null) {
            LOG.warning(() -> "Tool not found: " + toolName);
            return CompletableFuture.completedFuture(
                AgentDecision.fail("Tool not found: " + toolName)
            );
        }

        // --- 2. Schema validation ---
        String validationError = validateArguments(tool, invocation.arguments());
        if (validationError != null) {
            LOG.warning(() -> "Schema validation failed for tool " + toolName + ": " + validationError);
            return CompletableFuture.completedFuture(
                AgentDecision.fail("Schema validation failed: " + validationError)
            );
        }

        // --- 3. Approval policy ---
        if (requiresApproval(tool, invocation)) {
            LOG.info(() -> "Tool " + toolName + " requires human approval.");
            return CompletableFuture.completedFuture(
                new AgentDecision.WaitForApproval(invocation)
            );
        }

        // --- 4. Guardrail check (optional CDI bean from wayang-guardrails-runtime) ---
        ToolGuardrailCheck guardrailCheck = resolveGuardrailCheck();
        if (guardrailCheck != null) {
            ToolGuardrailCheck.Result guardResult =
                guardrailCheck.check(toolName, invocation.arguments());
            if (!guardResult.allowed()) {
                LOG.warning(() -> "Guardrail blocked tool " + toolName
                    + ": " + guardResult.reason()
                    + " (policies: " + guardResult.triggeredPolicies() + ")");
                return CompletableFuture.completedFuture(
                    AgentDecision.fail("Guardrail blocked: " + guardResult.reason())
                );
            }
        }

        // --- 5–8. Circuit breaker + retry + timeout + actual execution ---
        return CompletableFuture.supplyAsync(() -> runWithResilience(tool, invocation));
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private Tool resolveTool(String name) {
        if (toolInstances == null) return null;
        for (Tool t : toolInstances) {
            if (name.equals(t.descriptor().name())) {
                return t;
            }
        }
        return null;
    }

    private String validateArguments(Tool tool, Map<String, Object> arguments) {
        if (tool.descriptor() == null) return null;
        tech.kayys.wayang.tool.ToolDescriptor descriptor = tool.descriptor();
        // If the descriptor carries required field names, verify them.
        if (descriptor.inputSchema() != null) {
            // inputSchema is a JSON object string — do a lightweight required-field check.
            String schema = descriptor.inputSchema().toString();
            if (schema.contains("\"required\"")) {
                // Extract required fields via simple string scan (avoids pulling in a JSON lib).
                int reqIdx = schema.indexOf("\"required\"");
                int arrStart = schema.indexOf('[', reqIdx);
                int arrEnd = schema.indexOf(']', arrStart);
                if (arrStart >= 0 && arrEnd > arrStart) {
                    String reqSection = schema.substring(arrStart + 1, arrEnd);
                    for (String tok : reqSection.split(",")) {
                        String field = tok.replaceAll("[\"\\[\\]\\s]", "").trim();
                        if (!field.isEmpty() && (arguments == null || !arguments.containsKey(field))) {
                            return "missing required field: " + field;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Tools that mutate state (filesystem writes, API calls, shell commands) need approval
     * when the agent is not in auto-approve mode.  We detect mutation by convention on the
     * tool name prefix.  A proper capability-based check can replace this later.
     */
    private boolean requiresApproval(Tool tool, ToolInvocation invocation) {
        String name = invocation.name();
        return name.startsWith("filesystem.write")
            || name.startsWith("shell.")
            || name.startsWith("http.post")
            || name.startsWith("http.put")
            || name.startsWith("http.delete");
    }

    private AgentDecision runWithResilience(Tool tool, ToolInvocation invocation) {
        String toolName = invocation.name();
        var cb = circuitBreakerRegistry.getOrCreate(toolName);

        try {
            ToolResult result = cb.execute(() ->
                Retry.retry(() -> executeWithTimeout(tool, invocation), RETRY_POLICY)
            );
            LOG.fine(() -> "Tool " + toolName + " succeeded: success=" + result.isSuccess());
            return new AgentDecision.ToolCompleted(invocation, result);

        } catch (tech.kayys.wayang.resilience.CircuitBreakerOpenException cbOpen) {
            LOG.warning(() -> "Circuit breaker OPEN for tool " + toolName);
            return AgentDecision.fail("Tool " + toolName + " circuit breaker is open — too many failures.");

        } catch (TimeoutException te) {
            LOG.warning(() -> "Tool " + toolName + " timed out after " + TOOL_TIMEOUT_SECONDS + "s.");
            return AgentDecision.fail("Tool " + toolName + " timed out.");

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Tool " + toolName + " failed after retries", e);
            return AgentDecision.fail("Tool " + toolName + " failed: " + e.getMessage());
        }
    }

    private ToolResult executeWithTimeout(Tool tool, ToolInvocation invocation)
            throws Exception {
        var future = tool.execute(invocation, null);
        try {
            return future.get(TOOL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (java.util.concurrent.ExecutionException ee) {
            throw (Exception) ee.getCause();
        }
    }

    /**
     * Returns the first available {@link ToolGuardrailCheck} bean, or {@code null}
     * when {@code wayang-guardrails-runtime} is not deployed.
     */
    private ToolGuardrailCheck resolveGuardrailCheck() {
        if (guardrailCheckInstances == null || guardrailCheckInstances.isUnsatisfied()) {
            return null;
        }
        return guardrailCheckInstances.get();
    }
}
