package tech.kayys.wayang.mcp.client.runtime.config;


import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

import java.time.Duration;
import java.util.Map;

@ConfigMapping(prefix = "quarkus.mcp")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface MCPRuntimeConfig {

    /**
     * MCP server configurations
     */
    @WithName("servers")
    Map<String, MCPServerConfig> servers();

    /**
     * Whether to wait for all MCP connections to initialize before continuing startup
     */
    @WithName("wait-for-initialization")
    @WithDefault("true")
    boolean waitForInitialization();

    /**
     * Timeout for initialization when wait-for-initialization is enabled
     */
    @WithName("initialization-timeout")
    @WithDefault("PT30S")
    Duration initializationTimeout();

    /**
     * Global connection timeout
     */
    @WithName("connection-timeout")
    @WithDefault("PT10S")
    Duration connectionTimeout();

    /**
     * Global request timeout
     */
    @WithName("request-timeout")
    @WithDefault("PT30S")
    Duration requestTimeout();

    /**
     * Enable MCP client metrics
     */
    @WithName("metrics.enabled")
    @WithDefault("true")
    boolean metricsEnabled();

    /**
     * Enable MCP client health checks
     */
    @WithName("health.enabled")
    @WithDefault("true")
    boolean healthEnabled();
}
