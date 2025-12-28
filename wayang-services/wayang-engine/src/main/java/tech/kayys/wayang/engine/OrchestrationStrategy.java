package tech.kayys.wayang.engine;

/**
 * Orchestration strategy for multi-agent workflows.
 */
public enum OrchestrationStrategy {
    SEQUENTIAL, // Agents execute in defined order
    PARALLEL, // Agents execute concurrently
    CONDITIONAL, // Execution path determined by conditions
    DYNAMIC, // Orchestrator agent plans execution at runtime
    PLANNER_EXECUTOR, // Separate planning and execution phases
    MAP_REDUCE // Parallel map with aggregation reduce
}
