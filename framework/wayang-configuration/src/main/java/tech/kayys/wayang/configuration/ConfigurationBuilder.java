package tech.kayys.wayang.configuration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Configuration Builder - Fluent API
 */
public class ConfigurationBuilder {
    
    private final Map<String, Object> values = new LinkedHashMap<>();
    private final List<ConfigurationSource> sources = new ArrayList<>();
    
    public ConfigurationBuilder value(String path, Object value) {
        setValue(path, value);
        return this;
    }
    
    private void setValue(String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = values;
        
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            current = (Map<String, Object>) current.computeIfAbsent(part, k -> new LinkedHashMap<>());
        }
        
        current.put(parts[parts.length - 1], value);
    }
    
    public ConfigurationBuilder source(ConfigurationSource source) {
        this.sources.add(source);
        return this;
    }
    
    public ConfigurationBuilder file(Path path) throws Exception {
        return source(new FileConfigurationSource(path));
    }
    
    public ConfigurationBuilder environment() {
        return source(new EnvironmentConfigurationSource());
    }
    
    public ConfigurationBuilder systemProperties() {
        return source(new SystemPropertiesConfigurationSource());
    }
    
    public Configuration build() {
        Map<String, Object> merged = new LinkedHashMap<>(values);
        
        for (ConfigurationSource source : sources) {
            try {
                Map<String, Object> sourceValues = source.load();
                deepMerge(merged, sourceValues);
            } catch (Exception e) {
                System.err.println("Failed to load configuration source: " + e.getMessage());
            }
        }
        
        return new DefaultConfiguration(merged);
    }
    
    @SuppressWarnings("unchecked")
    private void deepMerge(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map && target.containsKey(key) && target.get(key) instanceof Map) {
                deepMerge((Map<String, Object>) target.get(key), (Map<String, Object>) value);
            } else {
                target.put(key, value);
            }
        }
    }
}
