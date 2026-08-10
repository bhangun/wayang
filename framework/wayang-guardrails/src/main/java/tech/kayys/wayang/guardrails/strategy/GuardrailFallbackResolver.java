package tech.kayys.wayang.guardrails.strategy;

import tech.kayys.wayang.agent.Agent;
import tech.kayys.wayang.tool.ToolInvocation;

/**
 * Resolves the appropriate fallback strategy to use when a guardrail violation occurs.
 */
public interface GuardrailFallbackResolver {
    
    /**
     * Resolves the strategy based on the current context.
     *
     * @param agent the agent executing the tool
     * @param invocation the tool invocation that triggered the violation
     * @return the resolved strategy
     */
    GuardrailFallbackStrategy resolve(Agent agent, ToolInvocation invocation);
}
