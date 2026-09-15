package tech.kayys.wayang.coordination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

/**
 * Registry of available coordination strategies discovered via CDI.
 */
@ApplicationScoped
public class CoordinationRegistry {

    private final Map<String, CoordinationStrategy> strategiesById;
    private final Map<CoordinationPattern, List<CoordinationStrategy>> strategiesByPattern;

    @Inject
    public CoordinationRegistry(Instance<CoordinationStrategy> instances) {
        this.strategiesById = new LinkedHashMap<>();

        if (instances != null) {
            for (CoordinationStrategy strategy : instances) {
                register(strategy);
            }
        }

        this.strategiesByPattern = buildPatternIndex(strategiesById);
    }

    private void register(CoordinationStrategy strategy) {
        Objects.requireNonNull(strategy, "strategy must not be null");

        String id = normalizeId(strategy.id());
        if (id.isBlank()) {
            throw new IllegalStateException("Coordination strategy id must not be blank: " + strategy.getClass().getName());
        }

        if (strategy.pattern() == null) {
            throw new IllegalStateException("Coordination strategy '" + id + "' must define a pattern");
        }

        CoordinationStrategy existing = strategiesById.putIfAbsent(id, strategy);
        if (existing != null) {
            throw new IllegalStateException("Duplicate coordination strategy id '" + id + "': "
                    + existing.getClass().getName() + " and " + strategy.getClass().getName());
        }
    }

    private Map<CoordinationPattern, List<CoordinationStrategy>> buildPatternIndex(
            Map<String, CoordinationStrategy> strategies) {
        Map<CoordinationPattern, List<CoordinationStrategy>> index = new LinkedHashMap<>();

        for (CoordinationStrategy strategy : strategies.values()) {
            index.computeIfAbsent(strategy.pattern(), ignored -> new ArrayList<>()).add(strategy);
        }

        index.replaceAll((pattern, list) -> List.copyOf(list));
        return Collections.unmodifiableMap(index);
    }

    public CoordinationStrategy get(String id) {
        String normalized = normalizeId(id);
        CoordinationStrategy strategy = strategiesById.get(normalized);
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported coordination strategy: '" + id
                    + "'. Available strategies: " + strategiesById.keySet());
        }
        return strategy;
    }

    public CoordinationStrategy find(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return strategiesById.get(normalizeId(id));
    }

    public List<CoordinationStrategy> findByPattern(CoordinationPattern pattern) {
        if (pattern == null) {
            return List.of();
        }
        return strategiesByPattern.getOrDefault(pattern, List.of());
    }

    public boolean contains(String id) {
        return find(id) != null;
    }

    public List<CoordinationStrategy> all() {
        return List.copyOf(strategiesById.values());
    }

    public List<String> ids() {
        return List.copyOf(strategiesById.keySet());
    }

    public int size() {
        return strategiesById.size();
    }

    private String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
