package tech.kayys.wayang.agent.orchestrator;

/**
 * Orchestration State
 */
public enum OrchestrationState {
    PLANNING,
    EXECUTING,
    COORDINATING,
    EVALUATING,
    COMPLETED,
    FAILED,
    CANCELLED
}
