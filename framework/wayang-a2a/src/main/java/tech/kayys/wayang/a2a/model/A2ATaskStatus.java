package tech.kayys.wayang.a2a.model;

/**
 * Represents the status of an A2A task.
 */
public enum A2ATaskStatus {
    PENDING,
    RUNNING,
    WAITING_FOR_TOOL,
    WAITING_FOR_APPROVAL,
    COMPLETED,
    FAILED,
    CANCELLED
}
