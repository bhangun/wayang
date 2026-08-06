package tech.kayys.wayang.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultToolExecutor implements ToolExecutor {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, ToolDescriptor> tools = new ConcurrentHashMap<>();
    
    public DefaultToolExecutor() {
        this.id = Id.random().asString();
        this.name = "default-tool-executor";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Tool Executor")
            .version(version)
            .label("type", "tool")
            .now()
            .build();
        
        // Register default tools
        tools.put("search", ToolDescriptor.of("search", "Search the web", ToolType.API));
        tools.put("calculate", ToolDescriptor.of("calculate", "Perform calculations", ToolType.FUNCTION));
        tools.put("translate", ToolDescriptor.of("translate", "Translate text", ToolType.API));
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("tool"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public ToolResult execute(ToolInvocation invocation) throws Exception {
        long startTime = System.currentTimeMillis();
        
        String name = invocation.name();
        Map<String, Object> params = invocation.parameters();
        
        Object result;
        switch (name.toLowerCase()) {
            case "search":
                String query = (String) params.getOrDefault("query", "default search");
                result = "Search results for: " + query;
                break;
            case "calculate":
                double a = ((Number) params.getOrDefault("a", 0)).doubleValue();
                double b = ((Number) params.getOrDefault("b", 0)).doubleValue();
                String operation = (String) params.getOrDefault("operation", "add");
                result = switch (operation) {
                    case "add" -> a + b;
                    case "subtract" -> a - b;
                    case "multiply" -> a * b;
                    case "divide" -> b != 0 ? a / b : "Error: Division by zero";
                    default -> "Unknown operation";
                };
                break;
            case "translate":
                String text = (String) params.getOrDefault("text", "");
                String target = (String) params.getOrDefault("target", "en");
                result = "Translated to " + target + ": " + text;
                break;
            default:
                result = "Tool not found: " + name;
        }
        
        long endTime = System.currentTimeMillis();
        
        return new ToolResult(
            Id.random().asString(),
            null,
            name,
            result,
            List.of(),
            Map.of(),
            true,
            null,
            endTime - startTime,
            startTime,
            endTime
        );
    }
    
    @Override
    public List<ToolDescriptor> listTools() {
        return new ArrayList<>(tools.values());
    }
}