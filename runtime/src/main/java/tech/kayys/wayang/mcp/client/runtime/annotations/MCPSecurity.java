package tech.kayys.wayang.mcp.client.runtime.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.util.Nonbinding;

/**
 * Configures security for MCP operations
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPSecurity {
    /**
     * Required roles
     */
    @Nonbinding
    String[] roles() default {};
    
    /**
     * Required permissions
     */
    @Nonbinding
    String[] permissions() default {};
    
    /**
     * Authentication type
     */
    @Nonbinding
    AuthType authType() default AuthType.NONE;
    
    enum AuthType {
        NONE,
        BASIC,
        BEARER,
        OAUTH,
        CUSTOM
    }
}
