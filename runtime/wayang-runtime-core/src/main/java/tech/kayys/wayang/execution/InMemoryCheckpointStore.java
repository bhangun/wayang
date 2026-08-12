package tech.kayys.wayang.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import tech.kayys.wayang.agent.AgentContext;
import tech.kayys.wayang.execution.cache.ExecutionCache;
import tech.kayys.wayang.execution.cache.ExecutionCacheEntry;

/**
 * Default in-memory implementation of {@link CheckpointStore}.
 *
 * <p>In addition to storing {@link AgentContext} snapshots, this implementation
 * records which {@link ExecutionCacheEntry} IDs were live at checkpoint time.
 * This allows a resumed execution to restore or warm the cache from a known-good
 * point rather than re-executing every tool call from scratch.</p>
 */
@ApplicationScoped
public class InMemoryCheckpointStore implements CheckpointStore {

    /** Primary checkpoint store: executionId → ordered list of contexts */
    private final Map<String, List<AgentContext>> store = new ConcurrentHashMap<>();

    /**
     * Cache references recorded alongside each checkpoint.
     * Key: executionId → list of cache entry IDs at that checkpoint.
     */
    private final Map<String, List<String>> cacheRefs = new ConcurrentHashMap<>();

    /** Optional cache — used to populate {@code cacheRefs} on save. */
    @Inject
    Instance<ExecutionCache> executionCacheInstances;

    // ── CheckpointStore ───────────────────────────────────────────────────────

    @Override
    public void save(String executionId, AgentContext context) {
        store.computeIfAbsent(executionId, k -> new CopyOnWriteArrayList<>()).add(context);

        // Record cache entry IDs live at this checkpoint for resume warmup
        ExecutionCache cache = resolveCache();
        if (cache != null) {
            List<String> entryIds = cache.listByExecution(executionId)
                    .stream()
                    .map(ExecutionCacheEntry::cacheId)
                    .collect(java.util.stream.Collectors.toList());
            if (!entryIds.isEmpty()) {
                cacheRefs.put(executionId, entryIds);
            }
        }
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
        return history != null ? new ArrayList<>(history) : Collections.emptyList();
    }

    @Override
    public void delete(String executionId) {
        store.remove(executionId);
        cacheRefs.remove(executionId);
    }

    // ── Cache reference query ─────────────────────────────────────────────────

    /**
     * Returns the cache entry IDs that were recorded at the last checkpoint for
     * the given execution.  Used during resume to warm the cache.
     *
     * @param executionId The execution to query
     * @return List of cache entry IDs; empty if none were recorded
     */
    public List<String> loadCacheRefs(String executionId) {
        return cacheRefs.getOrDefault(executionId, Collections.emptyList());
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private ExecutionCache resolveCache() {
        if (executionCacheInstances == null || executionCacheInstances.isUnsatisfied()) {
            return null;
        }
        return executionCacheInstances.get();
    }
}
