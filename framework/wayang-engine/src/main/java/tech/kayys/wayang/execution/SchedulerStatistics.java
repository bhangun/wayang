package tech.kayys.wayang.execution;

import java.util.Map;

/**
 * Statistics for the execution scheduler.
 */
public final class SchedulerStatistics {

    private final long totalScheduled;
    private final long totalCompleted;
    private final long totalFailed;
    private final long averageQueueTimeMs;
    private final long averageExecutionTimeMs;
    private final int maxConcurrent;
    private final int currentConcurrent;
    private final Map<String, Long> nodeTypeCounts;

    public SchedulerStatistics(long totalScheduled, long totalCompleted, long totalFailed,
            long averageQueueTimeMs, long averageExecutionTimeMs,
            int maxConcurrent, int currentConcurrent,
            Map<String, Long> nodeTypeCounts) {
        this.totalScheduled = totalScheduled;
        this.totalCompleted = totalCompleted;
        this.totalFailed = totalFailed;
        this.averageQueueTimeMs = averageQueueTimeMs;
        this.averageExecutionTimeMs = averageExecutionTimeMs;
        this.maxConcurrent = maxConcurrent;
        this.currentConcurrent = currentConcurrent;
        this.nodeTypeCounts = nodeTypeCounts != null ? Map.copyOf(nodeTypeCounts) : Map.of();
    }

    public long getTotalScheduled() {
        return totalScheduled;
    }

    public long getTotalCompleted() {
        return totalCompleted;
    }

    public long getTotalFailed() {
        return totalFailed;
    }

    public long getAverageQueueTimeMs() {
        return averageQueueTimeMs;
    }

    public long getAverageExecutionTimeMs() {
        return averageExecutionTimeMs;
    }

    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    public int getCurrentConcurrent() {
        return currentConcurrent;
    }

    public Map<String, Long> getNodeTypeCounts() {
        return nodeTypeCounts;
    }

    public double getSuccessRate() {
        long total = totalCompleted + totalFailed;
        return total > 0 ? (double) totalCompleted / total : 0.0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalScheduled;
        private long totalCompleted;
        private long totalFailed;
        private long averageQueueTimeMs;
        private long averageExecutionTimeMs;
        private int maxConcurrent;
        private int currentConcurrent;
        private Map<String, Long> nodeTypeCounts;

        public Builder totalScheduled(long totalScheduled) {
            this.totalScheduled = totalScheduled;
            return this;
        }

        public Builder totalCompleted(long totalCompleted) {
            this.totalCompleted = totalCompleted;
            return this;
        }

        public Builder totalFailed(long totalFailed) {
            this.totalFailed = totalFailed;
            return this;
        }

        public Builder averageQueueTimeMs(long averageQueueTimeMs) {
            this.averageQueueTimeMs = averageQueueTimeMs;
            return this;
        }

        public Builder averageExecutionTimeMs(long averageExecutionTimeMs) {
            this.averageExecutionTimeMs = averageExecutionTimeMs;
            return this;
        }

        public Builder maxConcurrent(int maxConcurrent) {
            this.maxConcurrent = maxConcurrent;
            return this;
        }

        public Builder currentConcurrent(int currentConcurrent) {
            this.currentConcurrent = currentConcurrent;
            return this;
        }

        public Builder nodeTypeCounts(Map<String, Long> nodeTypeCounts) {
            this.nodeTypeCounts = nodeTypeCounts;
            return this;
        }

        public SchedulerStatistics build() {
            return new SchedulerStatistics(totalScheduled, totalCompleted, totalFailed,
                    averageQueueTimeMs, averageExecutionTimeMs,
                    maxConcurrent, currentConcurrent, nodeTypeCounts);
        }
    }
}