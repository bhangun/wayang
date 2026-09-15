package tech.kayys.wayang.execution.lifecycle;

public record CancelExecution(
    String executionId
) implements ExecutionLifecycleCommand {
}
