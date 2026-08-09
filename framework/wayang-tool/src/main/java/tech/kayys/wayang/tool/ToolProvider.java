package tech.kayys.wayang.tool;

import java.util.List;

/**
 * SPI for dynamically supplying tools to the ToolRegistry.
 * Implementations (like MCP or OS providers) should register via ServiceLoader
 * to be automatically discovered.
 */
public interface ToolProvider {
    /**
     * @return the list of tools supplied by this provider.
     */
    List<Tool> getTools();
}
