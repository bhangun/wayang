package tech.kayys.wayang.execution;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.agent.AgentResponse;
import java.util.concurrent.CompletionStage;

/**
 * Permanent lifecycle bridge interface.
 * Connects the physical execution state with semantic agent state.
 */
public interface AgentExecution {
    
    /**
     * @return The unique execution ID.
     */
    String id();
    
    /**
     * @return The semantic state of the agent.
     */
    AgentContext agentContext();
    
    /**
     * @return The physical execution state.
     */
    ExecutionContext executionContext();
    
    /**
     * @return The current status of this execution.
     */
    ExecutionStatus status();
    
    /**
     * Starts or resumes the execution of this agent.
     * @return A future representing the final response of the agent.
     */
    CompletionStage<AgentResponse> execute();
    
    /**
     * Pauses this execution.
     */
    void pause();
    
    /**
     * Resumes this execution.
     */
    void resume();
    
    /**
     * Cancels this execution.
     */
    void cancel();
}
