package tech.kayys.wayang.execution;

/**
 * Represents the lifecycle stages of an agent execution.
 *
 * <p>Transitions: CREATED → AUTHORIZING → AUTHORIZED → WAITING_OBLIGATION → RUNNING → COMPLETED/FAILED/CANCELLED
 * Suspension may occur from RUNNING → SUSPENDED → RESUMING → RUNNING
 */
public enum ExecutionLifecycle {

    /** Invocation created but not yet processed. */
    CREATED,

    /** Authorization check in progress. */
    AUTHORIZING,

    /** Authorization succeeded; about to process obligations. */
    AUTHORIZED,

    /** Waiting for an obligation to complete (e.g. HITL approval, rate-limit window). */
    WAITING_OBLIGATION,

    /** Agent is actively executing. */
    RUNNING,

    /** Execution paused durably, pending external resume signal. */
    SUSPENDED,

    /** Resume signal received; about to resume execution. */
    RESUMING,

    /** Execution completed successfully. */
    COMPLETED,

    /** Execution failed with an error. */
    FAILED,

    /** Execution was cancelled by an explicit cancellation request. */
    CANCELLED
}
