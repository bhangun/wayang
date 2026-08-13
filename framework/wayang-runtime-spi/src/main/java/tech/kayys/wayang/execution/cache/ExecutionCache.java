package tech.kayys.wayang.execution.cache;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * SPI for the execution-scoped cache.
 *
 * <p>This is a first-class runtime abstraction, distinct from the project/code-scoped
 * {@code CacheStore} in {@code wayang-cache}.  While {@code CacheStore} is keyed on
 * artifacts and code versions, {@code ExecutionCache} is keyed on <em>runtime inputs</em>
 * (tool arguments, query embeddings, etc.) and carries provenance back to the execution
 * that produced the result.</p>
 *
 * <h2>Profiles</h2>
 * <ul>
 *   <li><b>Standalone / individual</b> – tenant and user keys are absent; cache is
 *       keyed on {@code (namespace, inputHash)} only.</li>
 *   <li><b>Enterprise</b> – tenant and user keys isolate entries between organisations
 *       and users; the same tool call by different tenants never shares a result.</li>
 * </ul>
 *
 * <h2>Namespaces</h2>
 * <ul>
 *   <li>{@link CacheNamespace#TOOL}      – tool invocation results</li>
 *   <li>{@link CacheNamespace#RETRIEVAL} – RAG / vector / keyword search results</li>
 *   <li>{@link CacheNamespace#CONTEXT}   – compiled prompt-context blocks</li>
 *   <li>{@link CacheNamespace#RESEARCH}  – full research task ledger entries</li>
 *   <li>{@link CacheNamespace#MODEL}     – model response for identical prompt+params</li>
 * </ul>
 */
public interface ExecutionCache {

    // ── Lookup ────────────────────────────────────────────────────────────────

    /**
     * Looks up a cached entry in the given namespace for the given input hash.
     * Tenant and user scope are derived from the store's current context (null = global).
     *
     * @param namespace  The cache namespace
     * @param tenantId   Tenant identifier — {@code null} in standalone mode
     * @param userId     User identifier   — {@code null} when not applicable
     * @param inputHash  Deterministic hash of the normalised inputs
     * @return The cached entry if present <em>and valid</em> (not expired)
     */
    Optional<ExecutionCacheEntry> lookup(CacheNamespace namespace,
                                         String tenantId,
                                         String userId,
                                         String inputHash);

    /**
     * Convenience lookup with no tenant/user scope (standalone mode).
     */
    default Optional<ExecutionCacheEntry> lookup(CacheNamespace namespace, String inputHash) {
        return lookup(namespace, null, null, inputHash);
    }

    // ── Store ─────────────────────────────────────────────────────────────────

    /**
     * Stores a cache entry.  If an unexpired entry with the same
     * {@code (namespace, tenantId, userId, inputHash)} already exists it is replaced.
     *
     * @param entry The entry to store
     */
    void store(ExecutionCacheEntry entry);

    // ── Invalidation ──────────────────────────────────────────────────────────

    /**
     * Invalidates all entries produced by a specific execution.
     * Useful when an execution is cancelled or its results are known to be stale.
     *
     * @param executionId The execution whose entries should be evicted
     */
    void invalidateByExecution(String executionId);

    /**
     * Invalidates all entries in a namespace for a given tenant.
     * Pass {@code null} for tenantId to invalidate the global/standalone namespace.
     *
     * @param namespace The namespace to clear
     * @param tenantId  Tenant scope, or {@code null} for standalone
     */
    void invalidateByNamespace(CacheNamespace namespace, String tenantId);

    // ── Query ─────────────────────────────────────────────────────────────────

    /**
     * Returns all (non-expired) cache entries produced by a given execution.
     * Used by checkpointing to record cache references alongside execution state.
     *
     * @param executionId The execution ID
     * @return List of entries, empty if none
     */
    List<ExecutionCacheEntry> listByExecution(String executionId);

    // ── Statistics ────────────────────────────────────────────────────────────

    /**
     * Returns a snapshot of cache statistics for observability and debugging.
     */
    CacheStats stats();

    /**
     * Lightweight cache statistics snapshot.
     *
     * @param totalEntries  Total number of live (non-expired) entries in the store
     * @param hitCount      Cumulative number of cache hits since the store started
     * @param missCount     Cumulative number of cache misses
     * @param evictionCount Cumulative number of expired/invalidated evictions
     */
    record CacheStats(long totalEntries, long hitCount, long missCount, long evictionCount) {

        public double hitRate() {
            long total = hitCount + missCount;
            return total == 0 ? 0.0 : (double) hitCount / total;
        }
    }

    // ── Hash helpers ──────────────────────────────────────────────────────────

    /**
     * Computes a deterministic SHA-256 input hash from a map of arguments.
     * Entries are sorted by key before hashing so argument order is irrelevant.
     */
    static String hashInputs(java.util.Map<String, Object> arguments) {
        try {
            var sorted = new java.util.TreeMap<>(arguments == null
                    ? java.util.Map.of() : arguments);
            String canonical = sorted.toString();
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            var sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the JVM spec — this never happens
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    /**
     * Computes a deterministic SHA-256 hash from a plain string (e.g. a RAG query).
     */
    static String hashString(String input) {
        return hashInputs(java.util.Map.of("_", input == null ? "" : input));
    }

    /**
     * Builds a compound input hash that includes a tool name prefix for readability.
     * Format: {@code <toolName>:<inputsHash>}
     */
    static String hashTool(String toolName, java.util.Map<String, Object> arguments) {
        return toolName + ":" + hashInputs(arguments);
    }

    /**
     * Builds a default cache TTL based on the execution budget.
     * Returns {@code null} if caching is disabled in the budget.
     */
    static java.time.Instant expiresAt(java.time.Duration ttl) {
        return ttl == null ? null : java.time.Instant.now().plus(ttl);
    }
}
