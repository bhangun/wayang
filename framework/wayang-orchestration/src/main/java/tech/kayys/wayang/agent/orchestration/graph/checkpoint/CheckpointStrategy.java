package tech.kayys.wayang.agent.orchestration.graph.checkpoint;

import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;

/**
 * SPI for persisting and loading workflow state, allowing workflows 
 * to be paused, inspected, and resumed across node executions.
 */
public interface CheckpointStrategy {
    
    /**
     * Saves the current graph state for a specific execution thread.
     */
    void save(String threadId, GraphState state);

    /**
     * Loads the last known graph state for a specific execution thread.
     * @return the state, or null if no checkpoint exists
     */
    GraphState load(String threadId);
}
