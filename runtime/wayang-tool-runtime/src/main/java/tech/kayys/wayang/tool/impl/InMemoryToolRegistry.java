package tech.kayys.wayang.tool.impl;

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
