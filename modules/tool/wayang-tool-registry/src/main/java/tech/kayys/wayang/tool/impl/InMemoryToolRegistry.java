package tech.kayys.wayang.tool.impl;

import tech.kayys.wayang.tool.Tool;
import tech.kayys.wayang.tool.ToolProvider;
import tech.kayys.wayang.tool.ToolRegistry;
import tech.kayys.wayang.tool.capability.CapabilityRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ToolRegistry with ServiceLoader auto-discovery.
 */
@ApplicationScoped
public class InMemoryToolRegistry implements ToolRegistry {

    private final Map<String, Tool> tools = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Automatically discover and register tools from all ToolProviders on the classpath
        ServiceLoader<ToolProvider> providers = ServiceLoader.load(ToolProvider.class);
        for (ToolProvider provider : providers) {
            registerProvider(provider);
        }
    }

    @Override
    public void register(Tool tool) {
        tools.put(tool.descriptor().name(), tool);
    }

    @Override
    public Optional<Tool> findByName(String name) {
        return Optional.ofNullable(tools.get(name));
    }

    @Override
    public List<Tool> listTools() {
        return new ArrayList<>(tools.values());
    }

    @Override
    public List<Tool> getToolsByCapability(CapabilityRequest request) {
        return tools.values().stream()
                .filter(t -> request.isSatisfiedBy(t.capabilities()))
                .collect(Collectors.toList());
    }

    @Override
    public void registerProvider(ToolProvider provider) {
        if (provider != null) {
            for (Tool t : provider.getTools()) {
                register(t);
            }
        }
    }

    @Override
    public String id() {
        return "in-memory-tool-registry";
    }

    @Override
    public void start() throws Exception {
        init();
    }

    @Override
    public void stop() throws Exception {
        tools.clear();
    }
}
