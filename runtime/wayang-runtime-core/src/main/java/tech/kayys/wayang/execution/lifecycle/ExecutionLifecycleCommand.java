package tech.kayys.wayang.execution.lifecycle;

public sealed interface ExecutionLifecycleCommand
    permits PauseExecution,
            ResumeExecution,
            CancelExecution,
            RetryExecution {

    String executionId();
}
