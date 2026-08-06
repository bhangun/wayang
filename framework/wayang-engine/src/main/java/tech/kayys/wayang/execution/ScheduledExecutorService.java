package tech.kayys.wayang.execution;

import java.time.Duration;
import java.time.Instant;

/**
 * Scheduled executor service for managing scheduled tasks.
 */
public interface ScheduledExecutorService {

    /**
     * Schedules a task.
     */
    ScheduledTask schedule(ScheduledTask task);

    /**
     * Schedules a one-time task.
     */
    ScheduledTask scheduleOnce(String name, Runnable task, Instant scheduledAt);

    /**
     * Schedules a recurring task.
     */
    ScheduledTask scheduleRecurring(String name, Runnable task, Instant scheduledAt, Duration interval);

    /**
     * Cancels a scheduled task.
     */
    void cancel(String taskId);

    /**
     * Cancels all scheduled tasks.
     */
    void cancelAll();

    /**
     * Returns all scheduled tasks.
     */
    java.util.List<ScheduledTask> getTasks();

    /**
     * Returns tasks due for execution.
     */
    java.util.List<ScheduledTask> getDueTasks();

    /**
     * Returns executor statistics.
     */
    ExecutorStatistics getStatistics();

    /**
     * Shuts down the executor.
     */
    void shutdown();

    /**
     * Shuts down the executor immediately.
     */
    void shutdownNow();

    /**
     * Checks if the executor is running.
     */
    boolean isRunning();

    /**
     * Executor statistics.
     */
    final class ExecutorStatistics {
        private final int totalTasks;
        private final int activeTasks;
        private final int completedTasks;
        private final int failedTasks;
        private final long totalExecutionTimeMs;

        public ExecutorStatistics(int totalTasks, int activeTasks, int completedTasks,
                int failedTasks, long totalExecutionTimeMs) {
            this.totalTasks = totalTasks;
            this.activeTasks = activeTasks;
            this.completedTasks = completedTasks;
            this.failedTasks = failedTasks;
            this.totalExecutionTimeMs = totalExecutionTimeMs;
        }

        public int getTotalTasks() {
            return totalTasks;
        }

        public int getActiveTasks() {
            return activeTasks;
        }

        public int getCompletedTasks() {
            return completedTasks;
        }

        public int getFailedTasks() {
            return failedTasks;
        }

        public long getTotalExecutionTimeMs() {
            return totalExecutionTimeMs;
        }

        public double getSuccessRate() {
            int total = completedTasks + failedTasks;
            return total > 0 ? (double) completedTasks / total : 0.0;
        }
    }
}