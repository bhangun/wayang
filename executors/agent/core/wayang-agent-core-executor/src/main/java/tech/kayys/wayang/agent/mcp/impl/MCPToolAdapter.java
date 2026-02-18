package tech.kayys.wayang.agent.mcp.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.agent.mcp.MCPToolProvider;
import tech.kayys.wayang.agent.mcp.model.MCPTool;
import tech.kayys.wayang.agent.model.Tool;
import tech.kayys.wayang.agent.model.ToolDefinition;
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
        return Uni.createFrom().item(() -> toolRegistry.getAllTools().stream()
                .map(this::convertToMCPTool)
                .collect(Collectors.toList()));
    }

    @Override
    public Uni<MCPTool> getTool(String name) {
        return Uni.createFrom().item(() -> {
            Tool tool = toolRegistry.getTool(name);
            if (tool == null) {
                throw new IllegalArgumentException("Tool not found: " + name);
            }
            return convertToMCPTool(tool);
        });
    }

    @Override
    public Uni<ToolResult> executeTool(String toolName, Map<String, Object> arguments) {
        return Uni.createFrom().item(() -> {
            Tool tool = toolRegistry.getTool(toolName);
            if (tool == null) {
                throw new IllegalArgumentException("Tool not found: " + toolName);
            }
            return tool.execute(arguments);
        });
    }

    @Override
    public Uni<Boolean> validateArguments(String toolName, Map<String, Object> arguments) {
        return Uni.createFrom().item(() -> {
            Tool tool = toolRegistry.getTool(toolName);
            if (tool == null) {
                return false;
            }

            ToolDefinition definition = tool.getDefinition();
            // Basic validation - check required parameters
            for (var param : definition.getParameters()) {
                if (param.isRequired() && !arguments.containsKey(param.getName())) {
                    return false;
                }
            }
            return true;
        });
    }

    /**
     * Convert internal Tool to MCP format.
     */
    private MCPTool convertToMCPTool(Tool tool) {
        ToolDefinition definition = tool.getDefinition();

        // Convert parameters to JSON Schema
        Map<String, Object> inputSchema = Map.of(
                "type", "object",
                "properties", convertParametersToSchema(definition),
                "required", getRequiredParameters(definition));

        return MCPTool.builder()
                .name(definition.getName())
                .description(definition.getDescription())
                .inputSchema(inputSchema)
                .annotations(Map.of(
                        "category", definition.getCategory(),
                        "version", "1.0"))
                .build();
    }

    private Map<String, Object> convertParametersToSchema(ToolDefinition definition) {
        return definition.getParameters().stream()
                .collect(Collectors.toMap(
                        param -> param.getName(),
                        param -> Map.of(
                                "type", mapTypeToJsonSchema(param.getType()),
                                "description", param.getDescription())));
    }

    private List<String> getRequiredParameters(ToolDefinition definition) {
        return definition.getParameters().stream()
                .filter(param -> param.isRequired())
                .map(param -> param.getName())
                .collect(Collectors.toList());
    }

    private String mapTypeToJsonSchema(String type) {
        return switch (type.toLowerCase()) {
            case "string" -> "string";
            case "integer", "int" -> "integer";
            case "number", "double", "float" -> "number";
            case "boolean", "bool" -> "boolean";
            case "array", "list" -> "array";
            case "object", "map" -> "object";
            default -> "string";
        };
    }
}
