package tech.kayys.wayang.guardrails.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.agent.spi.approval.ApprovalRequiredException;
import tech.kayys.wayang.guardrails.ExecutionResult;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * Defines what action to take when a tool invocation violates a guardrail policy.
 */
public interface GuardrailFallbackStrategy {
    
    /**
     * Handles the policy violation.
     * 
     * @param agent the agent executing the tool
     * @param invocation the tool being called
     * @param result the guardrail violation details
     * @throws ApprovalRequiredException if escalated to a human
     * @throws SecurityException if hard-blocked
     */
    void handleViolation(Agent agent, ToolInvocation invocation, ExecutionResult result) throws ApprovalRequiredException;
}
