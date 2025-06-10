package tech.kayys.wayang.mcp.client.runtime.client;

import java.util.Map;
import java.util.Optional;

import io.smallrye.config.WithDefault;

// Client info configuration
public interface MCPClientInfo {

    /**
     * Client name
     */
    @WithDefault("quarkus-mcp-client")
    String name();

    /**
     * Client version
     */
    @WithDefault("1.0.0")
    String version();

    /**
     * Additional client metadata
     */
    Map<String, Object> metadata();
}
