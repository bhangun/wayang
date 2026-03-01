package tech.kayys.wayang.agent.mcp.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.mcp.MCPToolProvider;
import tech.kayys.wayang.agent.mcp.model.MCPTool;
import tech.kayys.wayang.agent.model.AgentContext;
import tech.kayys.wayang.agent.model.Tool;
import tech.kayys.wayang.agent.model.ToolRegistry;
import tech.kayys.wayang.agent.model.ToolResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of MCPToolProvider that adapts existing Tool infrastructure to
 * MCP format.
 * Bridges wayang-agent-core-executor tools with MCP protocol.
 */
@ApplicationScoped
public class MCPToolAdapter implements MCPToolProvider {

    @Inject
    ToolRegistry toolRegistry;

    @Override
    public Uni<List<MCPTool>> listTools() {
        return toolRegistry.getAllTools("default")
                .map(tools -> tools.stream()
                        .map(this::convertToMCPTool)
                        .collect(Collectors.toList()));
    }

    @Override
    public Uni<MCPTool> getTool(String name) {
        return toolRegistry.getTool(name, "default")
                .map(tool -> {
                    if (tool == null) {
                        throw new IllegalArgumentException("Tool not found: " + name);
                    }
                    return convertToMCPTool(tool);
                });
    }

    @Override
    public Uni<ToolResult> executeTool(String toolName, Map<String, Object> arguments) {
        return toolRegistry.getTool(toolName, "default")
                .chain(tool -> {
                    if (tool == null) {
                        throw new IllegalArgumentException("Tool not found: " + toolName);
                    }
                    // Creating a dummy AgentContext for now, as it's required by the API
                    AgentContext context = AgentContext.builder()
                            .runId("mcp-run")
                            .nodeId("mcp-node")
                            .tenantId("default")
                            .build();
                    return tool.execute(arguments, context)
                            .map(output -> ToolResult.success("mcp-call", toolName, output));
                });
    }

    @Override
    public Uni<Boolean> validateArguments(String toolName, Map<String, Object> arguments) {
        return toolRegistry.getTool(toolName, "default")
                .chain(tool -> {
                    if (tool == null) {
                        return Uni.createFrom().item(false);
                    }
                    return tool.validate(arguments);
                });
    }

    /**
     * Convert internal Tool to MCP format.
     */
    private MCPTool convertToMCPTool(Tool tool) {
        Map<String, Object> inputSchema = tool.parameterSchema();

        return MCPTool.builder()
                .name(tool.name())
                .description(tool.description())
                .inputSchema(inputSchema != null ? inputSchema : Map.of("type", "object", "properties", Map.of()))
                .annotations(Map.of("version", (Object) "1.0"))
                .build();
    }
}
