package tech.kayys.wayang.execution.lifecycle;

public record PauseExecution(
    String executionId
) implements ExecutionLifecycleCommand {
}
