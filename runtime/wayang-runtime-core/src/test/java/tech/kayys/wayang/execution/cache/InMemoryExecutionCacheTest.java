package tech.kayys.wayang.execution.cache;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryExecutionCacheTest {

    private InMemoryExecutionCache cache;

    @BeforeEach
    void setUp() {
        cache = new InMemoryExecutionCache();
    }

    // ── Basic put / get ───────────────────────────────────────────────────────

    @Test
    @DisplayName("stores and retrieves a TOOL entry (standalone, no tenant/user)")
    void storeAndRetrieveStandalone() {
        String hash = ExecutionCache.hashTool("web.search", Map.of("query", "wayang"));
        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.TOOL)
                .inputHash(hash)
                .value("result-1")
                .executionId("exec-1")
                .build());

        Optional<ExecutionCacheEntry> hit = cache.lookup(CacheNamespace.TOOL, hash);
        assertTrue(hit.isPresent(), "should be a cache hit");
        assertEquals("result-1", hit.get().value());
        assertTrue(hit.get().cacheHit(), "asHit() should mark it as a hit");
    }

    @Test
    @DisplayName("miss returns empty")
    void missReturnsEmpty() {
        Optional<ExecutionCacheEntry> miss = cache.lookup(CacheNamespace.TOOL, "no-such-hash");
        assertTrue(miss.isEmpty());
    }

    // ── Expiry ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("expired entry is treated as a miss")
    void expiredEntryIsEvicted() {
        String hash = ExecutionCache.hashString("expire-me");
        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.RETRIEVAL)
                .inputHash(hash)
                .value("stale-result")
                .expiresAt(Instant.now().minusSeconds(1)) // already expired
                .build());

        Optional<ExecutionCacheEntry> hit = cache.lookup(CacheNamespace.RETRIEVAL, hash);
        assertTrue(hit.isEmpty(), "expired entry must be evicted on lookup");
    }

    // ── Namespace isolation ───────────────────────────────────────────────────

    @Test
    @DisplayName("same hash in different namespaces returns different entries")
    void namespacesAreIsolated() {
        String hash = ExecutionCache.hashString("shared-query");

        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.TOOL)
                .inputHash(hash)
                .value("tool-result")
                .build());

        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.RETRIEVAL)
                .inputHash(hash)
                .value("retrieval-result")
                .build());

        assertEquals("tool-result",      cache.lookup(CacheNamespace.TOOL, hash).get().value());
        assertEquals("retrieval-result", cache.lookup(CacheNamespace.RETRIEVAL, hash).get().value());
    }

    // ── Tenant / user isolation ───────────────────────────────────────────────

    @Test
    @DisplayName("entries from different tenants do not collide")
    void tenantIsolation() {
        String hash = ExecutionCache.hashTool("web.search", Map.of("query", "Wayang"));

        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.TOOL)
                .tenantId("tenant-a")
                .userId("user-1")
                .inputHash(hash)
                .value("result-for-tenant-a")
                .build());

        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.TOOL)
                .tenantId("tenant-b")
                .userId("user-2")
                .inputHash(hash)
                .value("result-for-tenant-b")
                .build());

        Optional<ExecutionCacheEntry> a = cache.lookup(CacheNamespace.TOOL, "tenant-a", "user-1", hash);
        Optional<ExecutionCacheEntry> b = cache.lookup(CacheNamespace.TOOL, "tenant-b", "user-2", hash);

        assertTrue(a.isPresent());
        assertTrue(b.isPresent());
        assertEquals("result-for-tenant-a", a.get().value());
        assertEquals("result-for-tenant-b", b.get().value());

        // Cross-tenant lookup should miss
        Optional<ExecutionCacheEntry> cross = cache.lookup(CacheNamespace.TOOL, "tenant-a", "user-2", hash);
        assertTrue(cross.isEmpty(), "cross-tenant lookup must return empty");
    }

    // ── listByExecution ───────────────────────────────────────────────────────

    @Test
    @DisplayName("listByExecution returns only entries for that execution")
    void listByExecution() {
        String hashA = ExecutionCache.hashString("query-a");
        String hashB = ExecutionCache.hashString("query-b");
        String hashC = ExecutionCache.hashString("query-c");

        cache.store(ExecutionCacheEntry.builder().namespace(CacheNamespace.TOOL).inputHash(hashA).executionId("exec-1").value("a").build());
        cache.store(ExecutionCacheEntry.builder().namespace(CacheNamespace.TOOL).inputHash(hashB).executionId("exec-1").value("b").build());
        cache.store(ExecutionCacheEntry.builder().namespace(CacheNamespace.TOOL).inputHash(hashC).executionId("exec-2").value("c").build());

        List<ExecutionCacheEntry> exec1Entries = cache.listByExecution("exec-1");
        assertEquals(2, exec1Entries.size());

        List<ExecutionCacheEntry> exec2Entries = cache.listByExecution("exec-2");
        assertEquals(1, exec2Entries.size());
        assertEquals("c", exec2Entries.get(0).value());
    }

    // ── Invalidation ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("invalidateByExecution removes all entries for that execution")
    void invalidateByExecution() {
        String hash = ExecutionCache.hashString("data");
        cache.store(ExecutionCacheEntry.builder()
                .namespace(CacheNamespace.TOOL)
                .inputHash(hash)
                .executionId("exec-99")
                .value("v")
                .build());

        cache.invalidateByExecution("exec-99");

        assertTrue(cache.lookup(CacheNamespace.TOOL, hash).isEmpty());
        assertTrue(cache.listByExecution("exec-99").isEmpty());
    }

    @Test
    @DisplayName("invalidateByNamespace removes entries for that namespace (global scope)")
    void invalidateByNamespace() {
        String hash1 = ExecutionCache.hashString("r1");
        String hash2 = ExecutionCache.hashString("r2");
        String hash3 = ExecutionCache.hashString("t1");

        cache.store(ExecutionCacheEntry.builder().namespace(CacheNamespace.RETRIEVAL).inputHash(hash1).value("r1").build());
        cache.store(ExecutionCacheEntry.builder().namespace(CacheNamespace.RETRIEVAL).inputHash(hash2).value("r2").build());
        cache.store(ExecutionCacheEntry.builder().namespace(CacheNamespace.TOOL).inputHash(hash3).value("t1").build());

        cache.invalidateByNamespace(CacheNamespace.RETRIEVAL, null);

        assertTrue(cache.lookup(CacheNamespace.RETRIEVAL, hash1).isEmpty());
        assertTrue(cache.lookup(CacheNamespace.RETRIEVAL, hash2).isEmpty());
        assertTrue(cache.lookup(CacheNamespace.TOOL, hash3).isPresent(), "TOOL entries must survive RETRIEVAL invalidation");
    }

    // ── Statistics ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("stats reflect hits and misses accurately")
    void statsReflectHitsAndMisses() {
        String hash = ExecutionCache.hashString("stats-test");
        cache.store(ExecutionCacheEntry.builder().namespace(CacheNamespace.TOOL).inputHash(hash).value("v").build());

        cache.lookup(CacheNamespace.TOOL, hash);   // hit
        cache.lookup(CacheNamespace.TOOL, "x");    // miss
        cache.lookup(CacheNamespace.TOOL, "y");    // miss

        ExecutionCache.CacheStats stats = cache.stats();
        assertEquals(1, stats.hitCount());
        assertEquals(2, stats.missCount());
        assertEquals(1.0 / 3.0, stats.hitRate(), 0.01);
    }

    // ── Hash helpers ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("hashInputs is stable and order-independent")
    void hashInputsStableAndOrderIndependent() {
        Map<String, Object> argsA = Map.of("b", 2, "a", 1);
        Map<String, Object> argsB = Map.of("a", 1, "b", 2);

        assertEquals(ExecutionCache.hashInputs(argsA), ExecutionCache.hashInputs(argsB),
                "argument order must not affect the hash");
    }
}
