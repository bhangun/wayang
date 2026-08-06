package tech.kayys.wayang.execution;

import java.util.UUID;

import tech.kayys.wayang.execution.ScheduledExecutorService.ExecutorStatistics;
import tech.kayys.wayang.harness.spi.ExecutionOptions;

/**
 * Executes execution graphs.
 */
public interface ExecutionExecutor {

    /**
     * Executes an execution graph.
     */
    ExecutionResult execute(ExecutionGraph graph);

    /**
     * Executes an execution graph with options.
     */
    ExecutionResult execute(ExecutionGraph graph, ExecutionOptions options);

    /**
     * Cancels an execution.
     */
    void cancel(UUID executionId);

    /**
     * Returns the status of an execution.
     */
    ExecutionStatus getStatus(UUID executionId);

    /**
     * Pauses an execution.
     */
    void pause(UUID executionId);

    /**
     * Resumes an execution.
     */
    void resume(UUID executionId);

    /**
     * Returns executor statistics.
     */
    ExecutorStatistics getStatistics();
}
