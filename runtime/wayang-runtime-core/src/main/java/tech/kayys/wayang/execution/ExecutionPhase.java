package tech.kayys.wayang.execution;

/**
 * The phase of an agent execution.
 */
public enum ExecutionPhase {
    GUARD,
    INPUT,
    CONTEXT,
    PLANNING,
    REASONING,
    INFERENCE,
    TOOL,
    MEMORY,
    EVALUATION,
    OUTPUT,
    COMPLETE
}
