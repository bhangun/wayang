package tech.kayys.wayang.execution;

/**
 * Execution status.
 */
public enum ExecutionStatus {
    PENDING("Execution is pending"),
    RUNNING("Execution is running"),
    COMPLETED("Execution completed successfully"),
    FAILED("Execution failed"),
    ERROR("Execution encountered an error"),
    CANCELLED("Execution was cancelled"),
    PAUSED("Execution is paused"),
    TIMEOUT("Execution timed out"),
    UNKNOWN("Execution status is unknown");

    private final String description;

    ExecutionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == ERROR ||
                this == CANCELLED || this == TIMEOUT;
    }

    public boolean isActive() {
        return this == RUNNING || this == PAUSED;
    }
}