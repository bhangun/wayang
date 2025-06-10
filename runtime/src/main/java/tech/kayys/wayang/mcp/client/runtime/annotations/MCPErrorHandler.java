package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Configures error handling for MCP operations
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPErrorHandler {
    /**
     * Error codes to handle
     */
    @Nonbinding
    int[] errorCodes() default {};
    
    /**
     * Exception types to handle
     */
    @Nonbinding
    Class<? extends Exception>[] exceptions() default {};
    
    /**
     * Fallback behavior
     */
    @Nonbinding
    FallbackBehavior fallback() default FallbackBehavior.THROW;
    
    /**
     * Custom error handler method name
     */
    @Nonbinding
    String handler() default "";
    
    enum FallbackBehavior {
        THROW,          // Throw the exception
        RETURN_NULL,    // Return null
        RETURN_EMPTY,   // Return empty result
        CUSTOM          // Use custom handler
    }
}
