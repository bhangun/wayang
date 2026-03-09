package tech.kayys.wayang.agent.mcp;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.agent.mcp.model.MCPTool;
import tech.kayys.wayang.agent.model.ToolResult;

import java.util.List;
import java.util.Map;

/**
 * MCP Tool Provider interface.
 * Adapts existing tools to MCP format.
 */
public interface MCPToolProvider {

    /**
     * List all available tools in MCP format.
     * 
     * @return List of MCP tools
     */
    Uni<List<MCPTool>> listTools();

    /**
     * Get a tool by name.
     * 
     * @param name The tool name
     * @return The MCP tool
     */
    Uni<MCPTool> getTool(String name);

    /**
     * Execute a tool call.
     * 
     * @param toolName  The tool name
     * @param arguments Tool arguments
     * @return Tool execution result
     */
    Uni<ToolResult> executeTool(String toolName, Map<String, Object> arguments);

    /**
     * Validate tool arguments against schema.
     * 
     * @param toolName  The tool name
     * @param arguments Arguments to validate
     * @return True if valid
     */
    Uni<Boolean> validateArguments(String toolName, Map<String, Object> arguments);
}
