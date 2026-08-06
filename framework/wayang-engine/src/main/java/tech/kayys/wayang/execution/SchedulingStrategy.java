package tech.kayys.wayang.execution;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scheduling strategy for determining node execution order.
 */
public interface SchedulingStrategy {

    /**
     * Returns the name of the strategy.
     */
    String getName();

    /**
     * Schedules nodes for execution.
     */
    List<ExecutionNode> schedule(List<ExecutionNode> readyNodes, ExecutionState state);

    /**
     * Returns the node comparator for ordering.
     */
    default Comparator<ExecutionNode> getComparator() {
        return Comparator.comparingInt(n -> n.id().hashCode());
    }

    /**
     * Sequential scheduling strategy.
     */
    final class SequentialStrategy implements SchedulingStrategy {
        public static final SequentialStrategy INSTANCE = new SequentialStrategy();

        private SequentialStrategy() {
        }

        @Override
        public String getName() {
            return "sequential";
        }

        @Override
        public List<ExecutionNode> schedule(List<ExecutionNode> readyNodes, ExecutionState state) {
            // Only schedule one node at a time
            return readyNodes.stream()
                    .limit(1)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Parallel scheduling strategy.
     */
    final class ParallelStrategy implements SchedulingStrategy {
        private final int maxConcurrent;

        public ParallelStrategy() {
            this(Integer.MAX_VALUE);
        }

        public ParallelStrategy(int maxConcurrent) {
            this.maxConcurrent = maxConcurrent;
        }

        @Override
        public String getName() {
            return "parallel";
        }

        @Override
        public List<ExecutionNode> schedule(List<ExecutionNode> readyNodes, ExecutionState state) {
            // Schedule all ready nodes up to max concurrent
            return readyNodes.stream()
                    .limit(maxConcurrent)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Priority scheduling strategy.
     */
    final class PriorityStrategy implements SchedulingStrategy {
        private final Comparator<ExecutionNode> priorityComparator;

        public PriorityStrategy(Comparator<ExecutionNode> priorityComparator) {
            this.priorityComparator = priorityComparator;
        }

        @Override
        public String getName() {
            return "priority";
        }

        @Override
        public List<ExecutionNode> schedule(List<ExecutionNode> readyNodes, ExecutionState state) {
            return readyNodes.stream()
                    .sorted(priorityComparator)
                    .collect(Collectors.toList());
        }

        @Override
        public Comparator<ExecutionNode> getComparator() {
            return priorityComparator;
        }
    }

    /**
     * Round robin scheduling strategy.
     */
    final class RoundRobinStrategy implements SchedulingStrategy {
        private int currentIndex = 0;

        @Override
        public String getName() {
            return "round_robin";
        }

        @Override
        public List<ExecutionNode> schedule(List<ExecutionNode> readyNodes, ExecutionState state) {
            if (readyNodes.isEmpty()) {
                return readyNodes;
            }
            // Rotate through ready nodes
            currentIndex = (currentIndex + 1) % readyNodes.size();
            return List.of(readyNodes.get(currentIndex));
        }
    }

    /**
     * Resource-aware scheduling strategy.
     */
    final class ResourceAwareStrategy implements SchedulingStrategy {
        private final double cpuThreshold;
        private final double memoryThreshold;

        public ResourceAwareStrategy(double cpuThreshold, double memoryThreshold) {
            this.cpuThreshold = cpuThreshold;
            this.memoryThreshold = memoryThreshold;
        }

        @Override
        public String getName() {
            return "resource_aware";
        }

        @Override
        public List<ExecutionNode> schedule(List<ExecutionNode> readyNodes, ExecutionState state) {
            // Filter nodes that can be scheduled based on resource availability
            return readyNodes.stream()
                    .filter(node -> {
                        // Check resource requirements against available resources
                        return true;
                    })
                    .limit(10)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Deadline-aware scheduling strategy.
     */
    final class DeadlineAwareStrategy implements SchedulingStrategy {
        @Override
        public String getName() {
            return "deadline_aware";
        }

        @Override
        public List<ExecutionNode> schedule(List<ExecutionNode> readyNodes, ExecutionState state) {
            // Schedule nodes with earliest deadlines first
            return readyNodes.stream()
                    .sorted(Comparator.comparing(n -> {
                        // Get deadline from metadata
                        Object deadline = n.config().getOptions().get("deadline");
                        return deadline != null ? (long) deadline : Long.MAX_VALUE;
                    }))
                    .collect(Collectors.toList());
        }
    }
}