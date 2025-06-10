package tech.kayys.wayang.mcp.client.runtime.annotations;

import jakarta.enterprise.util.Nonbinding;
import jakarta.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods as MCP method invocations.
 * This annotation enables declarative MCP method calls with automatic parameter mapping
 * and response handling.
 * 
 * Usage examples:
 * 
 * <pre>
 * {@code
 * @MCPMethod(server = "myserver", method = "tools/list")
 * CompletableFuture<ToolsListResponse> listTools();
 * 
 * @MCPMethod(server = "myserver", method = "tools/call", timeout = 60000)
 * CompletableFuture<ToolCallResponse> callTool(@MCPParam("name") String toolName, 
 *                                              @MCPParam("arguments") Map<String, Object> args);
 * 
 * @MCPMethod(server = "myserver", method = "resources/read")
 * CompletableFuture<ResourceContent> readResource(@MCPParam("uri") String resourceUri);
 * }
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier
public @interface MCPMethod {
    
    /**
     * The name of the MCP server to send the request to.
     * This should match a server name configured in the application properties.
     * 
     * @return the server name
     */
    @Nonbinding
    String server();
    
    /**
     * The MCP method name to invoke on the server.
     * This should be a valid MCP method name as defined in the MCP specification.
     * 
     * Common MCP methods include:
     * - "initialize" - Initialize the connection
     * - "tools/list" - List available tools
     * - "tools/call" - Call a specific tool
     * - "resources/list" - List available resources
     * - "resources/read" - Read a specific resource
     * - "prompts/list" - List available prompts
     * - "prompts/get" - Get a specific prompt
     * - "roots/list" - List filesystem roots
     * - "completion/complete" - Get completions
     * - "logging/setLevel" - Set logging level
     * 
     * @return the MCP method name
     */
    @Nonbinding
    String method();
    
    /**
     * Request timeout in milliseconds.
     * If not specified, defaults to 30000ms (30 seconds).
     * 
     * @return timeout in milliseconds
     */
    @Nonbinding
    long timeout() default 30000L;
    
    /**
     * Whether to retry the request on failure.
     * If true, the request will be retried according to the retry configuration.
     * 
     * @return true if retries should be attempted
     */
    @Nonbinding
    boolean retry() default false;
    
    /**
     * Maximum number of retry attempts if retry is enabled.
     * Only used when retry() is true.
     * 
     * @return maximum retry attempts
     */
    @Nonbinding
    int maxRetries() default 3;
    
    /**
     * Delay between retry attempts in milliseconds.
     * Only used when retry() is true.
     * 
     * @return retry delay in milliseconds
     */
    @Nonbinding
    long retryDelay() default 1000L;
    
    /**
     * Whether this method call should be asynchronous.
     * When true, the method must return a CompletableFuture.
     * When false, the method will block until completion.
     * 
     * @return true for async execution
     */
    @Nonbinding
    boolean async() default true;
    
    /**
     * Optional description of what this method does.
     * Used for documentation and debugging purposes.
     * 
     * @return method description
     */
    @Nonbinding
    String description() default "";
}
