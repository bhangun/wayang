package tech.kayys.wayang.agent.orchestration.graph.state;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The shared memory structure passed between agents in the workflow graph.
 */
public class GraphState {
    
    private final Map<String, Object> data = new ConcurrentHashMap<>();
    private final Map<String, StateReducer<Object>> reducers = new HashMap<>();

    public GraphState() {
    }

    /**
     * Registers a reducer for a specific key in the state.
     * If no reducer is registered for a key, it defaults to overwriting.
     */
    @SuppressWarnings("unchecked")
    public <T> GraphState withReducer(String key, StateReducer<T> reducer) {
        reducers.put(key, (StateReducer<Object>) reducer);
        return this;
    }

    /**
     * Applies a state update using the registered reducers.
     */
    public void apply(StateUpdate update) {
        if (update == null || update.getUpdates().isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : update.getUpdates().entrySet()) {
            String key = entry.getKey();
            Object newValue = entry.getValue();
            
            StateReducer<Object> reducer = reducers.getOrDefault(key, new OverwriteReducer());
            Object currentState = data.get(key);
            
            Object mergedValue = reducer.reduce(currentState, newValue);
            if (mergedValue != null) {
                data.put(key, mergedValue);
            } else {
                data.remove(key);
            }
        }
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }
}
