import os

# Fix SPI ToolExecutor
spi_executor = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/framework/wayang-tool/src/main/java/tech/kayys/wayang/tool/ToolExecutor.java"
with open(spi_executor, "w") as f:
    f.write("""package tech.kayys.wayang.tool;

import java.util.concurrent.CompletableFuture;

public interface ToolExecutor {
    CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context);
}
""")

# Fix DefaultToolExecutor
impl_executor = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/runtime/wayang-tool-runtime/src/main/java/tech/kayys/wayang/tool/impl/DefaultToolExecutor.java"
with open(impl_executor, "w") as f:
    f.write("""package tech.kayys.wayang.tool.impl;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.tool.ToolExecutor;
import tech.kayys.wayang.tool.ToolRegistry;
import tech.kayys.wayang.tool.ToolInvocation;
import tech.kayys.wayang.tool.ToolContext;
import tech.kayys.wayang.tool.ToolResult;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.validation.ToolArgumentValidator;

import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class DefaultToolExecutor implements ToolExecutor {

    @Inject
    ToolRegistry toolRegistry;
    
    private final ToolArgumentValidator validator = new ToolArgumentValidator();

    @Override
    public CompletableFuture<ToolResult> execute(ToolInvocation invocation, ToolContext context) {
        return toolRegistry.findByName(invocation.name())
                .map(tool -> {
                    try {
                        validator.validate(tool, invocation.arguments());
                        return tool.execute(invocation, context);
                    } catch (Exception e) {
                        return CompletableFuture.<ToolResult>failedFuture(e);
                    }
                })
                .orElseGet(() -> CompletableFuture.failedFuture(new IllegalArgumentException("Tool not found: " + invocation.name())));
    }
}
""")

# Fix InMemoryToolRegistry
impl_registry = "/Users/bhangun/Workspace/workkayys/Products/Wayang/wayang-platform/Families/wayang/runtime/wayang-tool-runtime/src/main/java/tech/kayys/wayang/tool/impl/InMemoryToolRegistry.java"
with open(impl_registry, "w") as f:
    f.write("""package tech.kayys.wayang.tool.impl;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolRegistry;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class InMemoryToolRegistry implements ToolRegistry {

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    @Override
    public void register(Tool tool) {
        tools.put(tool.descriptor().name(), tool);
    }
    
    @Override
    public void unregister(ResourceId id) {
        Tool tool = get(id).orElse(null);
        if (tool != null) {
            tools.remove(tool.descriptor().name());
        }
    }

    @Override
    public Optional<Tool> find(ResourceId id) {
        return get(id);
    }
    
    @Override
    public List<Tool> findAll() {
        return listTools();
    }
    
    @Override
    public List<Tool> findByType(ResourceType type) {
        return listTools();
    }
    
    @Override
    public List<Tool> findByLabel(String key, String value) {
        return listTools();
    }
    
    @Override
    public boolean exists(ResourceId id) {
        return get(id).isPresent();
    }
    
    @Override
    public boolean existsByName(String name) {
        return tools.containsKey(name);
    }
    
    @Override
    public int count() {
        return tools.size();
    }
    
    @Override
    public void clear() {
        tools.clear();
    }

    private Optional<Tool> get(ResourceId id) {
        return tools.values().stream().filter(t -> t.id().equals(id)).findFirst();
    }
    
    @Override
    public Optional<Tool> findByName(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    @Override
    public List<Tool> listTools() {
        return new ArrayList<>(tools.values());
    }
}
""")
print("Fixed files.")
