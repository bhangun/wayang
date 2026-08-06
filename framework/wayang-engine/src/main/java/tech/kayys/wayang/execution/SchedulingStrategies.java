package tech.kayys.wayang.execution;

import java.util.Comparator;

/**
 * Factory for creating scheduling strategies.
 */
public final class SchedulingStrategies {

    private SchedulingStrategies() {
        // Utility class - private constructor
    }

    /**
     * Returns a sequential scheduling strategy.
     */
    public static SchedulingStrategy sequential() {
        return SchedulingStrategy.SequentialStrategy.INSTANCE;
    }

    /**
     * Returns a parallel scheduling strategy.
     */
    public static SchedulingStrategy parallel() {
        return new SchedulingStrategy.ParallelStrategy();
    }

    /**
     * Returns a parallel scheduling strategy with max concurrent limit.
     */
    public static SchedulingStrategy parallel(int maxConcurrent) {
        return new SchedulingStrategy.ParallelStrategy(maxConcurrent);
    }

    /**
     * Returns a priority scheduling strategy.
     */
    public static SchedulingStrategy priority(Comparator<ExecutionNode> comparator) {
        return new SchedulingStrategy.PriorityStrategy(comparator);
    }

    /**
     * Returns a round-robin scheduling strategy.
     */
    public static SchedulingStrategy roundRobin() {
        return new SchedulingStrategy.RoundRobinStrategy();
    }

    /**
     * Returns a resource-aware scheduling strategy.
     */
    public static SchedulingStrategy resourceAware(double cpuThreshold, double memoryThreshold) {
        return new SchedulingStrategy.ResourceAwareStrategy(cpuThreshold, memoryThreshold);
    }

    /**
     * Returns a deadline-aware scheduling strategy.
     */
    public static SchedulingStrategy deadlineAware() {
        return new SchedulingStrategy.DeadlineAwareStrategy();
    }
}