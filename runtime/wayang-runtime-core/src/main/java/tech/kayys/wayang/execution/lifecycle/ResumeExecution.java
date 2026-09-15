package tech.kayys.wayang.execution.lifecycle;

public record ResumeExecution(
    String executionId
) implements ExecutionLifecycleCommand {
}
