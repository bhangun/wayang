package tech.kayys.wayang.agent.orchestrator;

/**
 * Execution Status
 */
public enum ExecutionStatus {
    PENDING,
    PLANNING,
    EXECUTING,
    SUCCESS,
    FAILED,
    TIMEOUT,
    CANCELLED,
    PARTIAL_SUCCESS
}