package tech.kayys.wayang.mcp.client.runtime.annotations;

import jakarta.enterprise.util.Nonbinding;
import java.lang.annotation.*;

/**
 * Marks a class as an MCP Client configuration
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MCPClient {
    /**
     * Client name identifier
     */
    @Nonbinding
    String name() default "";
    
    /**
     * MCP server connection configuration
     */
    @Nonbinding
    String serverUrl() default "";
    
    /**
     * Transport type (websocket, http, stdio)
     */
    @Nonbinding
    Transport transport() default Transport.WEBSOCKET;
    
    /**
     * Connection timeout in seconds
     */
    @Nonbinding
    int connectionTimeout() default 30;
    
    /**
     * Request timeout in seconds
     */
    @Nonbinding
    int requestTimeout() default 60;
    
    /**
     * Maximum number of retries for failed requests
     */
    @Nonbinding
    int maxRetries() default 3;
    
    /**
     * Delay between retries in milliseconds
     */
    @Nonbinding
    long retryDelay() default 1000;
    
    /**
     * Auto-initialize client on startup
     */
    @Nonbinding
    boolean autoInitialize() default true;
    
    /**
     * Client capabilities configuration
     */
    @Nonbinding
    String[] capabilities() default {};
    
    enum Transport {
        WEBSOCKET, HTTP, STDIO
    }
}