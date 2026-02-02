package tech.kayys.wayang.agent.orchestrator;

/**
 * Step Status
 */
public enum StepStatus {
    PENDING,
    READY,
    EXECUTING,
    COMPLETED,
    FAILED,
    SKIPPED
}
