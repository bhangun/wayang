package tech.kayys.wayang.agent.orchestration.graph.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an update to the GraphState produced by a GraphNode.
 */
public class StateUpdate {
    private final Map<String, Object> updates;

    public StateUpdate() {
        this.updates = new HashMap<>();
    }

    public StateUpdate(Map<String, Object> updates) {
        this.updates = new HashMap<>(updates);
    }

    public StateUpdate put(String key, Object value) {
        this.updates.put(key, value);
        return this;
    }

    public Map<String, Object> getUpdates() {
        return updates;
    }
}
