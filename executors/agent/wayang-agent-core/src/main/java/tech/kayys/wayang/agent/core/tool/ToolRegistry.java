package tech.kayys.wayang.agent.core.tool;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.tool.spi.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registry for managing available tools for agents.
 */
public class ToolRegistry {
    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, Tool> tools = new HashMap<>();

    @Inject
    public ToolRegistry(Instance<Tool> toolInstances) {
        for (Tool tool : toolInstances) {
            log.info("Registering tool: {}", tool.id());
            tools.put(tool.id(), tool);
        }
    }

    /**
     * Get a tool by ID.
     */
    public Optional<Tool> getTool(String toolId) {
        return Optional.ofNullable(tools.get(toolId));
    }

    /**
     * List all registered tools.
     */
    public List<Tool> listTools() {
        return List.copyOf(tools.values());
    }

    /**
     * Execute a tool by ID.
     */
    public Uni<Map<String, Object>> executeTool(String toolId, Map<String, Object> arguments,
            Map<String, Object> context) {
        return getTool(toolId)
                .map(tool -> tool.execute(arguments, context))
                .orElse(Uni.createFrom().failure(new IllegalArgumentException("Tool not found: " + toolId)));
    }

    /**
     * Get tool definitions for LLM context.
     */
    public List<Map<String, Object>> getToolDefinitions() {
        return tools.values().stream()
                .map(tool -> Map.of(
                        "name", tool.id(), // Using ID for name in function calling
                        "description", tool.description(),
                        "parameters", tool.inputSchema()))
                .collect(Collectors.toList());
    }
}
