package tech.kayys.wayang.guardrails.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;
import tech.kayys.wayang.agent.spi.approval.ApprovalStrategy;
import tech.kayys.wayang.guardrails.ExecutionResult;
import tech.kayys.wayang.guardrails.GuardrailsEngine;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * Intercepts tool calls and evaluates them against the GuardrailsEngine.
 * If a policy violation occurs, delegates to the configured fallback strategy.
 */
public class GuardrailApprovalStrategy implements ApprovalStrategy {

    private final GuardrailsEngine engine;
    private final GuardrailFallbackResolver fallbackResolver;

    public GuardrailApprovalStrategy(GuardrailsEngine engine, GuardrailFallbackResolver fallbackResolver) {
        this.engine = engine;
        this.fallbackResolver = fallbackResolver;
    }

    public GuardrailApprovalStrategy(GuardrailsEngine engine) {
        // Default to resolving dynamically, with HITL as the ultimate fallback
        this(engine, new DynamicGuardrailFallbackResolver());
    }

    @Override
    public void requestApproval(Agent agent, ToolInvocation invocation) throws ApprovalRequiredException {
        
        // Stringify the arguments for guardrail scanning
        String inputToScan = invocation.arguments().toString();

        // Check if there are any violations (e.g. Toxicity, PII, Prompt Injection)
        ExecutionResult result = engine.evaluate(inputToScan);

        if (!result.isAllowed()) { // Assuming ExecutionResult has some indication of failure
            GuardrailFallbackStrategy strategy = fallbackResolver.resolve(agent, invocation);
            strategy.handleViolation(agent, invocation, result);
        }
    }
}
