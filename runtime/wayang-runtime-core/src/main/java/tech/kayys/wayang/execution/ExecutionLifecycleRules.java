package tech.kayys.wayang.execution;

public final class ExecutionLifecycleRules {

    private ExecutionLifecycleRules() {
    }

    public static boolean canTransition(
            ExecutionStatus from,
            ExecutionStatus to) {

        if (from == null || to == null) {
            return false;
        }

        if (from == to) {
            return true;
        }

        return switch (from) {
            case PENDING ->
                to == ExecutionStatus.RUNNING
                    || to == ExecutionStatus.CANCELLED;

            case RUNNING ->
                to == ExecutionStatus.PAUSED
                    || to == ExecutionStatus.CANCELLED
                    || to == ExecutionStatus.FAILED
                    || to == ExecutionStatus.ERROR
                    || to == ExecutionStatus.TIMEOUT
                    || to == ExecutionStatus.COMPLETED;

            case PAUSED ->
                to == ExecutionStatus.RUNNING
                    || to == ExecutionStatus.CANCELLED;

            case FAILED, ERROR, TIMEOUT ->
                to == ExecutionStatus.RUNNING
                    || to == ExecutionStatus.CANCELLED;

            case COMPLETED,
                 CANCELLED,
                 UNKNOWN ->
                false;
        };
    }

    public static void requireTransition(
            String executionId,
            ExecutionStatus from,
            ExecutionStatus to) {

        if (!canTransition(from, to)) {
            throw new InvalidExecutionTransitionException(
                executionId,
                from,
                to
            );
        }
    }
}
