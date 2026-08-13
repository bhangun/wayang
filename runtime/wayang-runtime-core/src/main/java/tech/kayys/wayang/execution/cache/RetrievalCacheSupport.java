package tech.kayys.wayang.execution.cache;

import java.util.List;
import java.util.Optional;

import tech.kayys.wayang.execution.ExecutionBudget;

/**
 * Retrieval / RAG cache helper.
 *
 * <p>Wraps an {@link ExecutionCache} to provide a high-level API for caching
 * embedding-based vector search, keyword search, and full RAG results.</p>
 *
 * <p>Designed to be injected or constructed wherever retrieval happens
 * (e.g. in a {@code MemoryManager}, {@code VectorStore}, or
 * {@code ContextProvider} implementation).</p>
 *
 * <h2>Key design</h2>
 * <ul>
 *   <li>The cache key for a retrieval is {@code SHA-256(tenantId|*, userId|*, query text)}.</li>
 *   <li>In standalone mode both tenant and user are absent; the key is purely query-driven.</li>
 *   <li>In enterprise mode tenant + user isolation ensures that different tenants never share
 *       retrieved documents.</li>
 * </ul>
 */
public final class RetrievalCacheSupport {

    private final ExecutionCache cache;
    private final String executionId;
    private final String tenantId;
    private final String userId;
    private final ExecutionBudget budget;

    public RetrievalCacheSupport(ExecutionCache cache,
                                  String executionId,
                                  String tenantId,
                                  String userId,
                                  ExecutionBudget budget) {
        this.cache       = cache;
        this.executionId = executionId;
        this.tenantId    = tenantId;
        this.userId      = userId;
        this.budget      = budget != null ? budget : ExecutionBudget.balanced();
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    /**
     * Looks up a previously cached retrieval result for the given query.
     *
     * @param query The raw retrieval query (text, embedding input, etc.)
     * @return The cached list of result strings if present and valid; empty if cache miss
     */
    @SuppressWarnings("unchecked")
    public Optional<List<String>> lookup(String query) {
        if (cache == null || !budget.retrievalCacheEnabled()) return Optional.empty();

        String inputHash = ExecutionCache.hashString(query);
        Optional<ExecutionCacheEntry> hit = cache.lookup(
                CacheNamespace.RETRIEVAL, tenantId, userId, inputHash);

        if (hit.isPresent() && hit.get().value() instanceof List<?> v) {
            return Optional.of((List<String>) v);
        }
        return Optional.empty();
    }

    /**
     * Stores a retrieval result for the given query.
     *
     * @param query   The raw retrieval query
     * @param results The list of retrieved document strings / chunks
     */
    public void store(String query, List<String> results) {
        if (cache == null || !budget.retrievalCacheEnabled()) return;

        String inputHash = ExecutionCache.hashString(query);
        java.time.Instant expires = ExecutionCache.expiresAt(budget.retrievalCacheTtl());

        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.RETRIEVAL)
                .tenantId(tenantId)
                .userId(userId)
                .executionId(executionId)
                .inputHash(inputHash)
                .value(results)
                .expiresAt(expires)
                .provenance("retrieval:" + truncate(query, 80))
                .build());
    }

    // ── Research cache ────────────────────────────────────────────────────────

    /**
     * Looks up a cached research task summary for the given research query.
     *
     * @param researchQuery The research query / objective
     * @return The cached research summary if present and valid; empty otherwise
     */
    public Optional<String> lookupResearch(String researchQuery) {
        if (cache == null || !budget.researchCacheEnabled()) return Optional.empty();

        String inputHash = ExecutionCache.hashString("research:" + researchQuery);
        Optional<ExecutionCacheEntry> hit = cache.lookup(
                CacheNamespace.RESEARCH, tenantId, userId, inputHash);

        if (hit.isPresent() && hit.get().value() instanceof String s) {
            return Optional.of(s);
        }
        return Optional.empty();
    }

    /**
     * Stores a research task result for the given research query.
     *
     * @param researchQuery The research query / objective
     * @param summary       The synthesised research summary / findings
     */
    public void storeResearch(String researchQuery, String summary) {
        if (cache == null || !budget.researchCacheEnabled()) return;

        String inputHash = ExecutionCache.hashString("research:" + researchQuery);
        java.time.Instant expires = ExecutionCache.expiresAt(budget.researchCacheTtl());

        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.RESEARCH)
                .tenantId(tenantId)
                .userId(userId)
                .executionId(executionId)
                .inputHash(inputHash)
                .value(summary)
                .expiresAt(expires)
                .provenance("research:" + truncate(researchQuery, 80))
                .build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
