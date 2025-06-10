package tech.kayys.wayang.mcp.client.runtime.config;

import java.time.Duration;
import java.util.Optional;

import io.smallrye.config.WithDefault;
import tech.kayys.wayang.mcp.client.runtime.transport.MCPTransportConfig;
import tech.kayys.wayang.mcp.client.runtime.client.MCPClientInfo;

// Server configuration
public interface MCPServerConfig {

    /**
     * Transport configuration for this server
     */
    MCPTransportConfig transport();

    /**
     * Whether this server connection is required
     */
    @WithDefault("true")
    boolean required();

    /**
     * Server-specific connection timeout
     */
    Optional<Duration> connectionTimeout();

    /**
     * Server-specific request timeout
     */
    Optional<Duration> requestTimeout();

    /**
     * Maximum number of concurrent requests
     */
    @WithDefault("10")
    int maxConcurrentRequests();

    /**
     * Enable automatic reconnection
     */
    @WithDefault("true")
    boolean autoReconnect();

    /**
     * Reconnection delay
     */
    @WithDefault("PT5S")
    Duration reconnectDelay();

    /**
     * Maximum reconnection attempts
     */
    @WithDefault("5")
    int maxReconnectAttempts();

    /**
     * Client information to send to the server
     */
    Optional<MCPClientInfo> clientInfo();

    /**
     * Authentication configuration
     */
    Optional<MCPAuthConfig> auth();
}
