package tech.kayys.wayang.execution;

/**
 * Defines the operational behavior and limits for an agent execution run.
 */
public enum RuntimeBehavior {
    
    /**
     * Optimized for speed. Uses lower token limits, smaller context budgets,
     * and cheaper models.
     */
    FAST,

    /**
     * Optimized for deep reasoning. Uses maximum context budgets, high token limits,
     * and reasoning models.
     */
    THOROUGH,

    /**
     * Balanced configuration for general purpose use.
     */
    BALANCED,

    /**
     * Specific configuration for troubleshooting. Emits maximal logging 
     * and uses standard limits.
     */
    DEBUG;
}
