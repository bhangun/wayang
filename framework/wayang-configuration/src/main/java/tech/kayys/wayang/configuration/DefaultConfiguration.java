package tech.kayys.wayang.configuration;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Default Configuration Implementation
 */
public class DefaultConfiguration implements Configuration {
    
    protected final Map<String, Object> values;
    private final List<ConfigurationListener> listeners = new CopyOnWriteArrayList<>();
    private final ObjectMapper yamlMapper;
    private final ObjectMapper jsonMapper;
    
    public DefaultConfiguration(Map<String, Object> values) {
        this.values = new LinkedHashMap<>(values);
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
        this.jsonMapper = new ObjectMapper();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(String path, Class<T> type) {
        if (path == null || path.isEmpty()) {
            return (T) values;
        }
        
        String[] parts = path.split("\\.");
        Object current = values;
        
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
                if (current == null) {
                    return null;
                }
            } else if (current instanceof List && part.matches("\\d+")) {
                int index = Integer.parseInt(part);
                List<?> list = (List<?>) current;
                if (index >= list.size()) {
                    return null;
                }
                current = list.get(index);
            } else {
                return null;
            }
        }
        
        if (current == null) {
            return null;
        }
        
        // Convert to target type
        if (type.isInstance(current)) {
            return type.cast(current);
        }
        
        // Handle primitive conversions
        if (type == Integer.class || type == int.class) {
            if (current instanceof Number) {
                return (T) Integer.valueOf(((Number) current).intValue());
            }
        }
        if (type == Long.class || type == long.class) {
            if (current instanceof Number) {
                return (T) Long.valueOf(((Number) current).longValue());
            }
        }
        if (type == Double.class || type == double.class) {
            if (current instanceof Number) {
                return (T) Double.valueOf(((Number) current).doubleValue());
            }
        }
        if (type == Boolean.class || type == boolean.class) {
            if (current instanceof Boolean) {
                return (T) current;
            }
        }
        if (type == String.class) {
            return (T) current.toString();
        }
        
        return null;
    }
    
    @Override
    public <T> T get(String path, Class<T> type, T defaultValue) {
        T value = get(path, type);
        return value != null ? value : defaultValue;
    }
    
    @Override
    public boolean has(String path) {
        return get(path, Object.class) != null;
    }
    
    @Override
    public Configuration getSection(String path) {
        Map<String, Object> section = get(path, Map.class);
        if (section == null) {
            return new DefaultConfiguration(Map.of());
        }
        return new DefaultConfiguration(section);
    }
    
    @Override
    public List<String> getKeys() {
        return new ArrayList<>(values.keySet());
    }
    
    @Override
    public Map<String, Object> asMap() {
        return new LinkedHashMap<>(values);
    }
    
    @Override
    public void reload() throws Exception {
        // Override in subclasses that support reloading
        // Default implementation does nothing
    }
    
    @Override
    public void watch(ConfigurationListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public void unwatch(ConfigurationListener listener) {
        listeners.remove(listener);
    }
    
    protected void notifyListeners() {
        for (ConfigurationListener listener : listeners) {
            listener.onConfigurationChanged(this);
        }
    }
}
