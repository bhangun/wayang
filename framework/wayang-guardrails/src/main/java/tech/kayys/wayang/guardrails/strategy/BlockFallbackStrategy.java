package tech.kayys.wayang.guardrails.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;
import tech.kayys.wayang.guardrails.ExecutionResult;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * A fallback strategy that strictly blocks the execution by throwing a SecurityException.
 */
public class BlockFallbackStrategy implements GuardrailFallbackStrategy {

    @Override
    public void handleViolation(Agent agent, ToolInvocation invocation, ExecutionResult result) throws ApprovalRequiredException {
        throw new SecurityException("Guardrail violation: Execution blocked by policy. Details: " + result.toString());
    }
}
