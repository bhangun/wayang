package tech.kayys.wayang.agent.orchestration.graph.state;

/**
 * The default reducer behavior: simply overwrites the old value with the new value.
 */
public class OverwriteReducer implements StateReducer<Object> {

    @Override
    public Object reduce(Object currentState, Object update) {
        return update; // Last write wins
    }
}
