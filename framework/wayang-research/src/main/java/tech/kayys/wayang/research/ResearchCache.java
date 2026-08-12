package tech.kayys.wayang.research;

import tech.kayys.wayang.cache.CacheKey;

/**
 * A layered cache specific to research operations (request, retrieval, derived-content).
 */
public interface ResearchCache {
    
    /**
     * Checks if the research result for a specific cache key is already available.
     */
    boolean hasResult(CacheKey key);
    
    /**
     * Stores a research artifact in the cache.
     */
    void store(CacheKey key, ResearchArtifact artifact);
    
    /**
     * Retrieves a research artifact from the cache.
     */
    ResearchArtifact retrieve(CacheKey key);
}
