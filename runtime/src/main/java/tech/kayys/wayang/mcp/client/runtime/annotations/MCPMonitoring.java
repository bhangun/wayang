package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Configures monitoring for MCP operations
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPMonitoring {
    /**
     * Whether to collect metrics
     */
    @Nonbinding
    boolean metrics() default true;
    
    /**
     * Whether to trace requests
     */
    @Nonbinding
    boolean tracing() default true;
    
    /**
     * Custom metric name
     */
    @Nonbinding
    String metricName() default "";
    
    /**
     * Tags to add to metrics
     */
    @Nonbinding
    String[] tags() default {};
}
