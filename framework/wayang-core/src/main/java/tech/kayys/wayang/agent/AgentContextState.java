package tech.kayys.wayang.agent;

/**
 * AgentContextState - Represents the state of an agent context.
 */
public enum AgentContextState {
    /**
     * Context has been created but not yet validated.
     */
    INITIALIZED,
    
    /**
     * Context is being validated.
     */
    VALIDATING,
    
    /**
     * Context is loading data.
     */
    LOADING,
    
    /**
     * Context is in planning phase.
     */
    PLANNING,
    
    /**
     * Context is in reasoning phase.
     */
    REASONING,
    
    /**
     * Context is executing.
     */
    EXECUTING,
    
    /**
     * Context is evaluating results.
     */
    EVALUATING,
    
    /**
     * Context has completed successfully.
     */
    COMPLETED,
    
    /**
     * Context has failed.
     */
    FAILED,
    
    /**
     * Context has been cancelled.
     */
    CANCELLED,
    
    /**
     * Context is paused.
     */
    PAUSED,
    
    /**
     * Context is retrying.
     */
    RETRYING,
    
    /**
     * Context is in error state.
     */
    ERROR
}

