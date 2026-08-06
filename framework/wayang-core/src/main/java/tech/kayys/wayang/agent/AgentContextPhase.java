package tech.kayys.wayang.agent;

/**
 * AgentContextPhase - Represents the current phase of execution.
 */
public enum AgentContextPhase {
    /**
     * Initial phase before any processing.
     */
    INIT,
    
    /**
     * Trigger phase - receiving trigger events.
     */
    TRIGGER,
    
    /**
     * Input phase - receiving input.
     */
    INPUT,
    
    /**
     * Context phase - loading context data.
     */
    CONTEXT,
    
    /**
     * Planning phase - creating plans.
     */
    PLANNING,
    
    /**
     * Reasoning phase - performing reasoning.
     */
    REASONING,
    
    /**
     * Model/Inference phase - calling LLM.
     */
    INFERENCE,
    
    /**
     * Tool phase - executing tools.
     */
    TOOLS,
    
    /**
     * Memory phase - storing/retrieving memory.
     */
    MEMORY,
    
    /**
     * Evaluation phase - evaluating results.
     */
    EVALUATION,
    
    /**
     * Guardrail phase - safety checks.
     */
    GUARDRAIL,
    
    /**
     * Output phase - sending output.
     */
    OUTPUT,
    
    /**
     * Completion phase - finalizing execution.
     */
    COMPLETE,
    
    /**
     * Custom phase.
     */
    CUSTOM
}
