package tech.kayys.wayang.execution.cache;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Thread-safe, in-memory implementation of {@link ExecutionCache}.
 *
 * <p>Entries are stored in a {@link ConcurrentHashMap} keyed by a compound string
 * {@code "<namespace>:<tenantId|*>:<userId|*>:<inputHash>"}.  A background cleanup is
 * intentionally avoided; expired entries are evicted lazily on lookup and on
 * {@link #listByExecution}.</p>
 *
 * <p>This implementation is suitable for single-node deployments.  A distributed
 * deployment (Redis, Hazelcast, etc.) should provide an alternative {@code ApplicationScoped}
 * bean that also implements {@link ExecutionCache}.</p>
 */
@ApplicationScoped
public class InMemoryExecutionCache implements ExecutionCache {

    /** Primary store: composite key → entry */
    private final Map<String, ExecutionCacheEntry> store = new ConcurrentHashMap<>();

    /** Secondary index: executionId → list of composite keys */
    private final Map<String, List<String>> executionIndex = new ConcurrentHashMap<>();

    // ── Statistics ────────────────────────────────────────────────────────────
    private final LongAdder hits      = new LongAdder();
    private final LongAdder misses    = new LongAdder();
    private final LongAdder evictions = new LongAdder();

    // ── Key construction ──────────────────────────────────────────────────────

    private static String compositeKey(CacheNamespace namespace,
                                        String tenantId,
                                        String userId,
                                        String inputHash) {
        String t = (tenantId != null && !tenantId.isBlank()) ? tenantId : "*";
        String u = (userId   != null && !userId.isBlank())   ? userId   : "*";
        return namespace.name() + ":" + t + ":" + u + ":" + inputHash;
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    @Override
    public Optional<ExecutionCacheEntry> lookup(CacheNamespace namespace,
                                                 String tenantId,
                                                 String userId,
                                                 String inputHash) {
        String key = compositeKey(namespace, tenantId, userId, inputHash);
        ExecutionCacheEntry entry = store.get(key);

        if (entry == null) {
            misses.increment();
            return Optional.empty();
        }

        if (entry.isExpired()) {
            store.remove(key);
            removeFromExecutionIndex(entry.executionId(), key);
            evictions.increment();
            misses.increment();
            return Optional.empty();
        }

        hits.increment();
        return Optional.of(entry.asHit());
    }

    // ── Store ─────────────────────────────────────────────────────────────────

    @Override
    public void store(ExecutionCacheEntry entry) {
        String key = compositeKey(entry.namespace(), entry.tenantId(),
                                   entry.userId(), entry.inputHash());
        store.put(key, entry);

        // Update secondary index
        if (entry.executionId() != null) {
            executionIndex.computeIfAbsent(entry.executionId(),
                    k -> new ArrayList<>()).add(key);
        }
    }

    // ── Invalidation ──────────────────────────────────────────────────────────

    @Override
    public void invalidateByExecution(String executionId) {
        List<String> keys = executionIndex.remove(executionId);
        if (keys != null) {
            keys.forEach(k -> {
                if (store.remove(k) != null) evictions.increment();
            });
        }
    }

    @Override
    public void invalidateByNamespace(CacheNamespace namespace, String tenantId) {
        String prefix = namespace.name() + ":" + (tenantId != null ? tenantId : "*") + ":";
        store.entrySet().removeIf(e -> {
            if (e.getKey().startsWith(prefix)) {
                evictions.increment();
                // Clean execution index lazily — stale refs are harmless
                return true;
            }
            return false;
        });
    }

    // ── Query ─────────────────────────────────────────────────────────────────

    @Override
    public List<ExecutionCacheEntry> listByExecution(String executionId) {
        List<String> keys = executionIndex.getOrDefault(executionId, List.of());
        List<String> stale = new ArrayList<>();

        List<ExecutionCacheEntry> result = keys.stream()
                .map(store::get)
                .filter(e -> {
                    if (e == null) return false;
                    if (e.isExpired()) {
                        stale.add(compositeKey(e.namespace(), e.tenantId(),
                                               e.userId(), e.inputHash()));
                        evictions.increment();
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // Evict expired stale keys lazily
        stale.forEach(store::remove);

        return result;
    }

    // ── Statistics ────────────────────────────────────────────────────────────

    @Override
    public CacheStats stats() {
        // Count only non-expired entries
        long live = store.values().stream().filter(ExecutionCacheEntry::isValid).count();
        return new CacheStats(live, hits.longValue(), misses.longValue(), evictions.longValue());
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private void removeFromExecutionIndex(String executionId, String key) {
        if (executionId == null) return;
        List<String> keys = executionIndex.get(executionId);
        if (keys != null) {
            keys.remove(key);
            if (keys.isEmpty()) executionIndex.remove(executionId);
        }
    }
}
