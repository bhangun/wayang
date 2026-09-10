package tech.kayys.wayang.agent.orchestration;

import tech.kayys.wayang.provider.ToolSpec;
import tech.kayys.wayang.tool.Tool;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Converts canonical Wayang tools into inference-facing agent tool definitions.
 */
public final class ToolDefinitionMapper {

    private ToolDefinitionMapper() {
    }

    public static ToolSpec fromTool(Tool tool) {
        Objects.requireNonNull(tool, "tool");
        var desc = tool.descriptor();
        return new ToolSpec(
                desc != null && desc.name() != null ? desc.name() : "",
                desc != null && desc.description() != null ? desc.description() : "",
                safeSchema(desc != null ? desc.inputSchema() : null));
    }

    public static List<ToolSpec> fromTools(List<? extends Tool> tools) {
        if (tools == null || tools.isEmpty()) {
            return List.of();
        }
        return tools.stream()
                .filter(Objects::nonNull)
                .map(ToolDefinitionMapper::fromTool)
                .toList();
    }

    private static Map<String, Object> safeSchema(Map<String, Object> schema) {
        return schema == null ? Map.of() : Map.copyOf(schema);
    }
}
