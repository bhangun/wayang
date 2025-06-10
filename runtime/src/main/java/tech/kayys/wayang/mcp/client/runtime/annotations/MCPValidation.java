package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Configures validation for MCP operations
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPValidation {
    /**
     * JSON Schema for validation
     */
    @Nonbinding
    String schema() default "";
    
    /**
     * Validation groups
     */
    @Nonbinding
    Class<?>[] groups() default {};
    
    /**
     * Whether validation is strict
     */
    @Nonbinding
    boolean strict() default true;
}

