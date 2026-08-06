package tech.kayys.wayang.cache;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



/**
 * Cache Provider - provides caching.
 */
public interface CacheProvider extends Extension {
    
    /**
     * Get from cache
     */
    <T> T get(String key) throws Exception;
    
    /**
     * Get with type
     */
    default <T> T get(String key, Class<T> type) throws Exception {
        Object value = get(key);
        if (value != null && type.isInstance(value)) {
            return type.cast(value);
        }
        return null;
    }
    
    /**
     * Put in cache
     */
    void put(String key, Object value) throws Exception;
    
    /**
     * Put with TTL
     */
    default void put(String key, Object value, long ttlSeconds) throws Exception {
        put(key, value);
    }
    
    /**
     * Remove from cache
     */
    void remove(String key) throws Exception;
    
    /**
     * Check if key exists
     */
    boolean exists(String key) throws Exception;
    
    /**
     * Clear cache
     */
    void clear() throws Exception;
    
    /**
     * Get or compute
     */
    default <T> T getOrCompute(String key, CacheLoader<T> loader) throws Exception {
        T value = get(key);
        if (value != null) {
            return value;
        }
        value = loader.load();
        put(key, value);
        return value;
    }
    
    /**
     * Get cache stats
     */
    default CacheStats getStats() throws Exception {
        return new CacheStats(0, 0, 0, 0);
    }
}
