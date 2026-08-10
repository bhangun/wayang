package tech.kayys.wayang.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;

import tech.kayys.wayang.agent.AgentContext;

/**
 * Default in-memory implementation of CheckpointStore.
 */
@ApplicationScoped
public class InMemoryCheckpointStore implements CheckpointStore {

    private final Map<String, List<AgentContext>> store = new ConcurrentHashMap<>();

    @Override
    public void save(String executionId, AgentContext context) {
        store.computeIfAbsent(executionId, k -> new CopyOnWriteArrayList<>()).add(context);
    }

    @Override
    public Optional<AgentContext> load(String executionId) {
        List<AgentContext> history = store.get(executionId);
        if (history != null && !history.isEmpty()) {
            return Optional.of(history.get(history.size() - 1));
        }
        return Optional.empty();
    }

    @Override
    public List<AgentContext> history(String executionId) {
        List<AgentContext> history = store.get(executionId);
        if (history != null) {
            return new ArrayList<>(history);
        }
        return Collections.emptyList();
    }

    @Override
    public void delete(String executionId) {
        store.remove(executionId);
    }
}
