package tech.kayys.wayang.mcp.client.runtime.transport;

import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClient;
import java.time.Duration;

/**
 * Configuration for MCP transport instances
 */
public interface MCPTransportConfig {
    /**
     * Get the transport type
     * 
     * @return the transport type
     */
    MCPClient.Transport type();

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
    String command();

    /**
     * Get the connection timeout
     * 
     * @return the connection timeout
     */
    Duration connectionTimeout();

    /**
     * Get the read timeout
     * 
     * @return the read timeout
     */
    Duration readTimeout();

    /**
     * Check if auto-reconnect is enabled
     * 
     * @return true if auto-reconnect is enabled
     */
    boolean autoReconnect();

    /**
     * Get the maximum number of reconnect attempts
     * 
     * @return the maximum number of reconnect attempts
     */
    int maxReconnectAttempts();

    /**
     * Get the reconnection delay
     * 
     * @return the reconnection delay
     */
    Duration reconnectDelay();
}
