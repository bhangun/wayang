package tech.kayys.wayang.execution;

import tech.kayys.wayang.agent.AgentContext;

/**
 * Interface for deciding the next action in an agent execution lifecycle.
 */
public interface AgentStrategy {
    
    /**
     * Decides the next action based on the current context and execution state.
     * 
     * @param context The semantic agent state
     * @param status The physical execution status
     * @return The decision of what to do next
     */
    AgentDecision decide(AgentContext context, ExecutionStatus status);
}
