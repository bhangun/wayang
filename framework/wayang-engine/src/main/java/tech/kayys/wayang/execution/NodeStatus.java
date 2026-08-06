package tech.kayys.wayang.execution;

/**
 * The lifecycle status of an ExecutionNode.
 */
public enum NodeStatus {
    /**
     * Node has been created but not yet scheduled.
     */
    CREATED,

    /**
     * Node is ready to be executed (all dependencies satisfied).
     */
    READY,

    /**
     * Node is currently executing.
     */
    RUNNING,

    /**
     * Node is waiting for external input or resource.
     */
    WAITING,

    /**
     * Node completed successfully.
     */
    COMPLETED,

    /**
     * Node execution failed.
     */
    FAILED,

    /**
     * Node was skipped (condition not met or parent skipped).
     */
    SKIPPED,

    /**
     * Node execution was cancelled.
     */
    CANCELLED,

    /**
     * Node is paused (for human approval or debugging).
     */
    PAUSED,

    /**
     * Node is retrying after failure.
     */
    RETRYING
}