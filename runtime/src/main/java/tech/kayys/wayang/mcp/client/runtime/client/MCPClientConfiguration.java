package tech.kayys.wayang.mcp.client.runtime.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.mcp.client.runtime.config.MCPRuntimeConfig;
import tech.kayys.wayang.mcp.client.runtime.config.MCPServerConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import tech.kayys.wayang.mcp.client.runtime.annotations.MCPClient;

/**
 * Configuration for MCP clients
 */
@ApplicationScoped
public class MCPClientConfiguration {
    
    @Inject
    MCPRuntimeConfig runtimeConfig;
    
    private String serverUrl;
    private MCPClient.Transport transport = MCPClient.Transport.WEBSOCKET;
    private Duration connectionTimeout = Duration.ofSeconds(30);
    private Duration requestTimeout = Duration.ofSeconds(60);
    private int maxRetries = 3;
    private boolean autoInitialize = true;
    private Set<String> capabilities;
    private boolean autoReconnect = true;
    private Duration reconnectDelay = Duration.ofSeconds(5);
    private int maxReconnectAttempts = 10;
    private Map<String, String> headers = new HashMap<>();
    private boolean enableHeartbeat = true;
    private Duration heartbeatInterval = Duration.ofSeconds(30);
    private boolean enableCompression = false;
    private int maxMessageSize = 1024 * 1024; // 1MB
    
    private MCPClientConfiguration() {}
    
    // Getters
    public String getServerUrl() { return serverUrl; }
    public MCPClient.Transport getTransport() { return transport; }
    public Duration getConnectionTimeout() { return connectionTimeout; }
    public Duration getRequestTimeout() { return requestTimeout; }
    public int getMaxRetries() { return maxRetries; }
    public boolean isAutoInitialize() { return autoInitialize; }
    public Set<String> getCapabilities() { return capabilities; }
    public boolean isAutoReconnect() { return autoReconnect; }
    public Duration getReconnectDelay() { return reconnectDelay; }
    public int getMaxReconnectAttempts() { return maxReconnectAttempts; }
    public Map<String, String> getHeaders() { return new HashMap<>(headers); }
    public boolean isEnableHeartbeat() { return enableHeartbeat; }
    public Duration getHeartbeatInterval() { return heartbeatInterval; }
    public boolean isEnableCompression() { return enableCompression; }
    public int getMaxMessageSize() { return maxMessageSize; }
    
    /**
     * Validate configuration
     */
    public void validate() {
        if (serverUrl == null || serverUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Server URL is required");
        }
        if (connectionTimeout.isNegative() || connectionTimeout.isZero()) {
            throw new IllegalArgumentException("Connection timeout must be positive");
        }
        if (requestTimeout.isNegative() || requestTimeout.isZero()) {
            throw new IllegalArgumentException("Request timeout must be positive");
        }
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
        if (reconnectDelay.isNegative() || reconnectDelay.isZero()) {
            throw new IllegalArgumentException("Reconnect delay must be positive");
        }
        if (maxReconnectAttempts < 0) {
            throw new IllegalArgumentException("Max reconnect attempts cannot be negative");
        }
        if (heartbeatInterval.isNegative() || heartbeatInterval.isZero()) {
            throw new IllegalArgumentException("Heartbeat interval must be positive");
        }
        if (maxMessageSize <= 0) {
            throw new IllegalArgumentException("Max message size must be positive");
        }
    }
    
    /**
     * Builder for MCPClientConfiguration
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final MCPClientConfiguration config = new MCPClientConfiguration();
        
        public Builder serverUrl(String serverUrl) {
            config.serverUrl = serverUrl;
            return this;
        }
        
        public Builder transport(MCPClient.Transport transport) {
            config.transport = transport;
            return this;
        }
        
        public Builder connectionTimeout(Duration connectionTimeout) {
            config.connectionTimeout = connectionTimeout;
            return this;
        }
        
        public Builder requestTimeout(Duration requestTimeout) {
            config.requestTimeout = requestTimeout;
            return this;
        }
        
        public Builder maxRetries(int maxRetries) {
            config.maxRetries = maxRetries;
            return this;
        }
        
        public Builder autoInitialize(boolean autoInitialize) {
            config.autoInitialize = autoInitialize;
            return this;
        }
        
        public Builder capabilities(Set<String> capabilities) {
            config.capabilities = capabilities;
            return this;
        }
        
        public Builder autoReconnect(boolean autoReconnect) {
            config.autoReconnect = autoReconnect;
            return this;
        }
        
        public Builder reconnectDelay(Duration reconnectDelay) {
            config.reconnectDelay = reconnectDelay;
            return this;
        }
        
        public Builder maxReconnectAttempts(int maxReconnectAttempts) {
            config.maxReconnectAttempts = maxReconnectAttempts;
            return this;
        }
        
        public Builder header(String name, String value) {
            config.headers.put(name, value);
            return this;
        }
        
        public Builder headers(Map<String, String> headers) {
            config.headers.putAll(headers);
            return this;
        }
        
        public Builder enableHeartbeat(boolean enableHeartbeat) {
            config.enableHeartbeat = enableHeartbeat;
            return this;
        }
        
        public Builder heartbeatInterval(Duration heartbeatInterval) {
            config.heartbeatInterval = heartbeatInterval;
            return this;
        }
        
        public Builder enableCompression(boolean enableCompression) {
            config.enableCompression = enableCompression;
            return this;
        }
        
        public Builder maxMessageSize(int maxMessageSize) {
            config.maxMessageSize = maxMessageSize;
            return this;
        }
        
        public MCPClientConfiguration build() {
            config.validate();
            return config;
        }
    }
    
    /**
     * Get server configuration by name
     * 
     * @param serverName the server name
     * @return the server configuration
     */
    public Optional<MCPServerConfig> getServerConfig(String serverName) {
        return Optional.ofNullable(runtimeConfig.servers().get(serverName));
    }
    
    /**
     * Get all server configurations
     * 
     * @return map of server configurations
     */
    public Map<String, MCPServerConfig> getAllServerConfigs() {
        return runtimeConfig.servers();
    }
}
