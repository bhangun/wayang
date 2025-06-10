package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Configures caching for MCP operations
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPCache {
    /**
     * Cache key pattern
     */
    @Nonbinding
    String key() default "";
    
    /**
     * TTL in seconds
     */
    @Nonbinding
    int ttl() default 300;
    
    /**
     * Cache strategy
     */
    @Nonbinding
    CacheStrategy strategy() default CacheStrategy.READ_THROUGH;
    
    enum CacheStrategy {
        READ_THROUGH,   // Cache on read
        WRITE_THROUGH,  // Cache on write
        WRITE_BEHIND    // Async cache write
    }
}
