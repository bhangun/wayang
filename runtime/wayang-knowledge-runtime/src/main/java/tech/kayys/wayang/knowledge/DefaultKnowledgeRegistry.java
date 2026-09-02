package tech.kayys.wayang.knowledge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe knowledge registry.
 */
public class DefaultKnowledgeRegistry implements KnowledgeRegistry {

    private final Map<String, KnowledgeSource> sources = new ConcurrentHashMap<>();

    @Override
    public void register(KnowledgeSource source) {
        if (source == null) {
            throw new IllegalArgumentException("source must not be null");
        }
        sources.put(source.id(), source);
    }

    @Override
    public void unregister(String sourceId) {
        if (sourceId != null) {
            sources.remove(sourceId);
        }
    }

    @Override
    public Optional<KnowledgeSource> find(String sourceId) {
        return Optional.ofNullable(sources.get(sourceId));
    }

    @Override
    public List<KnowledgeSource> sources() {
        return List.copyOf(new ArrayList<>(sources.values()));
    }
}
