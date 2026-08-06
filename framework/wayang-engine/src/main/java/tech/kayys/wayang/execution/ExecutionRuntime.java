package tech.kayys.wayang.execution;

import java.util.UUID;

import tech.kayys.wayang.harness.spi.ExecutionOptions;

/**
 * Execution runtime for managing graph execution.
 */
public interface ExecutionRuntime extends Runtime {

    /**
     * Executes an execution graph.
     */
    ExecutionResult execute(ExecutionGraph graph);

    /**
     * Executes an execution graph with options.
     */
    ExecutionResult execute(ExecutionGraph graph, ExecutionOptions options);

    /**
     * Executes an execution graph asynchronously.
     */
    java.util.concurrent.CompletableFuture<ExecutionResult> executeAsync(ExecutionGraph graph);

    /**
     * Returns the execution executor.
     */
    ExecutionExecutor executor();

    /**
     * Returns the execution scheduler.
     */
    ExecutionScheduler scheduler();

    /**
     * Returns the snapshot manager.
     */
    SnapshotManager snapshotManager();

    /**
     * Returns the execution status.
     */
    ExecutionStatus getStatus(UUID executionId);

    /**
     * Cancels an execution.
     */
    void cancel(UUID executionId);

    /**
     * Pauses an execution.
     */
    void pause(UUID executionId);

    /**
     * Resumes an execution.
     */
    void resume(UUID executionId);

    /**
     * Returns execution statistics.
     */
    ExecutionStatistics getStatistics();

    /**
     * Clears all executions.
     */
    void clear();
}