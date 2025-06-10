package tech.kayys.wayang.mcp.client.runtime.transport;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import io.smallrye.config.WithDefault;

/**
 * Configuration for MCP transport instances
 */
public interface MCPTransportConfig {
    /**
     * Get the transport type
     * 
     * @return the transport type
     */
    MCPTransportType type();

    /**
     * Get the transport URL
     * Required for HTTP and WebSocket transports
     * 
     * @return the transport URL
     */
    String url();

    /**
     * Get the command for stdio transport
     * Required for STDIO transport
     * 
     * @return the command
     */
    Optional<String> command();

    /**
     * Get the connection timeout
     * 
     * @return the connection timeout
     */
    @WithDefault("PT30S")
    Duration connectionTimeout();

    /**
     * Get the read timeout
     * 
     * @return the read timeout
     */
    @WithDefault("PT60S")
    Duration readTimeout();

    /**
     * Check if auto-reconnect is enabled
     * 
     * @return true if auto-reconnect is enabled
     */
    @WithDefault("true")
    boolean autoReconnect();

    /**
     * Get the maximum number of reconnect attempts
     * 
     * @return the maximum number of reconnect attempts
     */
    @WithDefault("3")
    int maxReconnectAttempts();

    /**
     * Get the reconnection delay
     * 
     * @return the reconnection delay
     */
    @WithDefault("PT5S")
    Duration reconnectDelay();

    /**
     * Get additional headers
     * 
     * @return the additional headers
     */
    Map<String, String> headers();
}
