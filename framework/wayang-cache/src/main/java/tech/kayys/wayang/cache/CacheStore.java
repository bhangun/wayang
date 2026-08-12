package tech.kayys.wayang.cache;

import java.util.Optional;

/**
 * Defines operations for storing and retrieving temporary cached content.
 */
public interface CacheStore {

    /**
     * Stores an object in the cache.
     */
    void put(CacheKey key, Object value, CachePolicy policy);

    /**
     * Retrieves an object from the cache.
     */
    <T> Optional<T> get(CacheKey key, Class<T> type);

    /**
     * Removes an object from the cache.
     */
    void remove(CacheKey key);

    /**
     * Clears all cache entries for a specific project.
     */
    void clear(String projectId);
}
