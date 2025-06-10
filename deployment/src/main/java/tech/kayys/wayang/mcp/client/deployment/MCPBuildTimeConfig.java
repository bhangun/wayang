package tech.kayys.wayang.mcp.client.deployment;


import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

import java.util.Optional;

@ConfigRoot(name = "mcp", phase = ConfigPhase.BUILD_TIME)
public class MCPBuildTimeConfig {

    /**
     * Whether to enable MCP client support
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;

    /**
     * Whether to enable development mode features
     */
    @ConfigItem(defaultValue = "false")
    public boolean devMode;

    /**
     * Package names to scan for MCP client interfaces
     */
    @ConfigItem
    public Optional<String[]> scanPackages;

    /**
     * Whether to enable native image support optimizations
     */
    @ConfigItem(defaultValue = "true")
    public boolean nativeImageSupport;

    /**
     * Whether to generate detailed logging for MCP operations
     */
    @ConfigItem(defaultValue = "false")
    public boolean verboseLogging;

    /**
     * Whether to validate MCP client interfaces at build time
     */
    @ConfigItem(defaultValue = "true")
    public boolean validateInterfaces;

    /**
     * Maximum number of generated client implementations
     */
    @ConfigItem(defaultValue = "50")
    public int maxGeneratedClients;
}
