package tech.kayys.wayang.execution;

public class InvalidExecutionTransitionException
        extends IllegalStateException {

    private final String executionId;
    private final ExecutionStatus from;
    private final ExecutionStatus to;

    public InvalidExecutionTransitionException(
            String executionId,
            ExecutionStatus from,
            ExecutionStatus to) {

        super(
            "Invalid execution transition [" +
            executionId +
            "]: " +
            from +
            " -> " +
            to
        );

        this.executionId = executionId;
        this.from = from;
        this.to = to;
    }

    public String executionId() {
        return executionId;
    }

    public ExecutionStatus from() {
        return from;
    }

    public ExecutionStatus to() {
        return to;
    }
}
