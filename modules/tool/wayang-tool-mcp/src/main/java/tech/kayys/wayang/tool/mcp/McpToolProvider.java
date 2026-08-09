package tech.kayys.wayang.tool.mcp;

import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolProvider;

import java.util.Collections;
import java.util.List;

/**
 * Discovers and exposes MCP tools to the Wayang ToolRegistry.
 */
public class McpToolProvider implements ToolProvider {

    @Override
    public List<Tool> getTools() {
        // In a real implementation, this would query the local MCP Client registry
        // and map the discovered MCP server tools into Wayang Tool interfaces,
        // attaching the McpCapability to each.
        // For the POC, we return an empty list.
        return Collections.emptyList();
    }
}
