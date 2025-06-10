package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Marks a method as an MCP Tool handler
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPTool {
    /**
     * Tool name
     */
    @Nonbinding
    String name() default "";
    
    /**
     * Tool description
     */
    @Nonbinding
    String description() default "";
    
    /**
     * JSON Schema for input validation (as string)
     */
    @Nonbinding
    String inputSchema() default "";
    
    /**
     * Whether the tool is dangerous and requires confirmation
     */
    @Nonbinding
    boolean dangerous() default false;
    
    /**
     * Maximum execution time in seconds
     */
    @Nonbinding
    int timeout() default 30;
    
    /**
     * Whether to retry on failure
     */
    @Nonbinding
    boolean retryable() default true;
}
