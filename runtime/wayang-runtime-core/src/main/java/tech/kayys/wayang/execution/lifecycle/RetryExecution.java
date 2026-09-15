package tech.kayys.wayang.execution.lifecycle;

public record RetryExecution(
    String executionId
) implements ExecutionLifecycleCommand {
}
