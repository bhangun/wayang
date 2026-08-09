package tech.kayys.wayang.agent.orchestration.graph.state;

import java.util.ArrayList;
import java.util.List;

/**
 * A reducer that appends new items to a list rather than overwriting it.
 */
public class AppendReducer<T> implements StateReducer<List<T>> {

    @Override
    public List<T> reduce(List<T> currentState, List<T> update) {
        List<T> result = new ArrayList<>();
        if (currentState != null) {
            result.addAll(currentState);
        }
        if (update != null) {
            result.addAll(update);
        }
        return result;
    }
}
