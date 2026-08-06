package tech.kayys.wayang.execution;

import java.util.List;

/**
 * Schedules execution of nodes in a graph.
 */
public interface ExecutionScheduler {

    /**
     * Returns the next nodes to execute.
     */
    List<ExecutionNode> schedule(ExecutionGraph graph, ExecutionState state);

    /**
     * Returns nodes that are ready for execution.
     */
    List<ExecutionNode> getReadyNodes(ExecutionGraph graph, ExecutionState state);

    /**
     * Updates the state after a node completes.
     */
    void updateState(ExecutionGraph graph, ExecutionState state, NodeResult result);

    /**
     * Returns scheduler statistics.
     */
    SchedulerStatistics getStatistics();

    /**
     * Returns the scheduling strategy.
     */
    SchedulingStrategy getStrategy();

    /**
     * Sets the scheduling strategy.
     */
    void setStrategy(SchedulingStrategy strategy);
}