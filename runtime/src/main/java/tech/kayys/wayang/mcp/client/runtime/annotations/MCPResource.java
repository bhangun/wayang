package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Marks a method as an MCP Resource handler
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPResource {
    /**
     * Resource URI pattern to match
     */
    @Nonbinding
    String uri() default "";
    
    /**
     * Resource name
     */
    @Nonbinding
    String name() default "";
    
    /**
     * Resource description
     */
    @Nonbinding
    String description() default "";
    
    /**
     * MIME type of the resource
     */
    @Nonbinding
    String mimeType() default "";
    
    /**
     * Whether this resource supports subscription
     */
    @Nonbinding
    boolean subscribable() default false;
    
    /**
     * Cache TTL in seconds (0 = no cache)
     */
    @Nonbinding
    int cacheTtl() default 0;
}

