package tech.kayys.wayang.guardrails.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;
import tech.kayys.wayang.guardrails.ExecutionResult;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * Immediately blocks the tool execution and throws an exception, aborting the agent's action.
 */
public class HardBlockFallbackStrategy implements GuardrailFallbackStrategy {

    @Override
    public void handleViolation(Agent agent, ToolInvocation invocation, ExecutionResult result) throws ApprovalRequiredException {
        throw new SecurityException("Action blocked by Guardrails: " + result.toString());
    }
}
