package tech.kayys.wayang.execution.cache;

import java.time.Instant;

/**
 * A single cache entry scoped to an execution.
 *
 * <p>The key is the combination of {@code (namespace, tenantId, userId, inputHash)}.
 * In standalone / individual mode {@code tenantId} and {@code userId} are {@code null}
 * so the cache is keyed purely on the content hash — seamless across profiles.</p>
 *
 * <p>Enterprise deployments provide tenant and user identifiers so cache entries
 * remain fully isolated between organisations and individuals.</p>
 *
 * @param cacheId      Unique identifier for this cache record
 * @param namespace    What kind of work is cached ({@link CacheNamespace})
 * @param tenantId     Tenant scope — {@code null} in standalone mode
 * @param userId       User scope  — {@code null} when not applicable
 * @param executionId  The execution that originally produced this entry (provenance)
 * @param toolId       Tool identifier — {@code null} for non-TOOL entries
 * @param inputHash    Deterministic SHA-256 hex of the normalised inputs
 * @param value        The cached result (must be serialisable)
 * @param createdAt    Wall-clock instant when the entry was stored
 * @param expiresAt    Expiry instant — {@code null} means the entry never expires
 * @param cacheHit     {@code true} when retrieved from cache; {@code false} when freshly stored
 * @param provenance   Human-readable origin description (e.g. tool name + args summary)
 */
public record ExecutionCacheEntry(
        String cacheId,
        CacheNamespace namespace,
        String tenantId,
        String userId,
        String executionId,
        String toolId,
        String inputHash,
        Object value,
        Instant createdAt,
        Instant expiresAt,
        boolean cacheHit,
        String provenance
) {

    /** Convenience: has this entry expired relative to now? */
    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    /** Convenience: is this entry still valid? */
    public boolean isValid() {
        return !isExpired();
    }

    /**
     * Returns an identical record with {@code cacheHit = true}, used when returning
     * a cache hit to the caller.
     */
    public ExecutionCacheEntry asHit() {
        return new ExecutionCacheEntry(
                cacheId, namespace, tenantId, userId, executionId, toolId,
                inputHash, value, createdAt, expiresAt, true, provenance);
    }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String cacheId;
        private CacheNamespace namespace;
        private String tenantId;
        private String userId;
        private String executionId;
        private String toolId;
        private String inputHash;
        private Object value;
        private Instant createdAt = Instant.now();
        private Instant expiresAt;
        private boolean cacheHit = false;
        private String provenance;

        public Builder cacheId(String v)        { this.cacheId = v;      return this; }
        public Builder namespace(CacheNamespace v) { this.namespace = v; return this; }
        public Builder tenantId(String v)       { this.tenantId = v;     return this; }
        public Builder userId(String v)         { this.userId = v;       return this; }
        public Builder executionId(String v)    { this.executionId = v;  return this; }
        public Builder toolId(String v)         { this.toolId = v;       return this; }
        public Builder inputHash(String v)      { this.inputHash = v;    return this; }
        public Builder value(Object v)          { this.value = v;        return this; }
        public Builder createdAt(Instant v)     { this.createdAt = v;    return this; }
        public Builder expiresAt(Instant v)     { this.expiresAt = v;    return this; }
        public Builder cacheHit(boolean v)      { this.cacheHit = v;     return this; }
        public Builder provenance(String v)     { this.provenance = v;   return this; }

        public ExecutionCacheEntry build() {
            if (cacheId == null)   cacheId = java.util.UUID.randomUUID().toString();
            if (namespace == null) throw new IllegalStateException("namespace is required");
            if (inputHash == null) throw new IllegalStateException("inputHash is required");
            return new ExecutionCacheEntry(
                    cacheId, namespace, tenantId, userId, executionId, toolId,
                    inputHash, value, createdAt, expiresAt, cacheHit, provenance);
        }
    }
}
