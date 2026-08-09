package tech.kayys.wayang.agent.orchestration.graph.state;

/**
 * Defines how a specific state field should be updated when new data arrives.
 * 
 * @param <T> the type of the value being reduced
 */
public interface StateReducer<T> {
    
    /**
     * Reduces (merges) the new update into the current state value.
     *
     * @param currentState the existing value in the GraphState (can be null)
     * @param update the new value provided by a GraphNode (can be null)
     * @return the new merged value to be stored in the GraphState
     */
    T reduce(T currentState, T update);
}
