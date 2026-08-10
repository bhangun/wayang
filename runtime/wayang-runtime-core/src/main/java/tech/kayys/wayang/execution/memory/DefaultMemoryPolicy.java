package tech.kayys.wayang.execution.memory;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.AgentContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-process {@link MemoryPolicy} for development and testing.
 *
 * <p>Storage is keyed by {@code sessionId + ":" + layer}.
 * In a production system this would delegate to a vector store (Semantic),
 * a relational store (Episodic) or an in-memory store (Working).</p>
 */
@ApplicationScoped
public class DefaultMemoryPolicy implements MemoryPolicy {

    private final Map<String, List<String>> store = new ConcurrentHashMap<>();

    @Override
    public List<String> retrieve(AgentContext context, MemoryLayer layer, String query, int limit) {
        String key = key(context, layer);
        List<String> items = store.getOrDefault(key, List.of());
        // Naive LIFO retrieval — real impl would do embedding similarity search
        int from = Math.max(0, items.size() - limit);
        return new ArrayList<>(items.subList(from, items.size()));
    }

    @Override
    public void write(AgentContext context, MemoryLayer layer, String content) {
        String key = key(context, layer);
        store.computeIfAbsent(key, k -> new ArrayList<>()).add(content);
    }

    private String key(AgentContext context, MemoryLayer layer) {
        String sessionId = context.sessionId() != null ? context.sessionId() : "default";
        return sessionId + ":" + layer.name();
    }
}
