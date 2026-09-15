package tech.kayys.wayang.communication.task;

public enum TaskStatus {

    SUBMITTED,

    WORKING,

    INPUT_REQUIRED,

    AUTH_REQUIRED,

    COMPLETED,

    FAILED,

    CANCELED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELED;
    }
}
