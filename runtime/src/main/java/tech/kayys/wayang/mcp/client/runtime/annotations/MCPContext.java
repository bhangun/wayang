package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Marks a parameter as MCP context injection
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPContext {
    /**
     * Context type to inject
     */
    @Nonbinding
    ContextType value() default ContextType.REQUEST;
    
    enum ContextType {
        REQUEST,        // Current MCP request
        CLIENT,         // MCP client instance
        SESSION,        // Client session info
        CAPABILITIES    // Server capabilities
    }
}
