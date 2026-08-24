package tech.kayys.wayang.agent.mcp;

import java.nio.file.Path;
import java.util.List;

/**
 * SPI for providing user/environment-specific MCP configuration file locations.
 * Allows agents and extensions (such as Aljabr) to register custom MCP config paths
 * without the Wayang platform framework having hardcoded knowledge of downstream agents.
 */
public interface McpConfigPathProvider {

    /**
     * Returns a list of candidate user/system configuration file paths to check for MCP servers.
     */
    List<Path> userConfigPaths();
}
