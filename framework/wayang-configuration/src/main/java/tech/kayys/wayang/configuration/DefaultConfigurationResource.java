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


import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.BaseResource;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.extension.Id;

/**
 * Configuration Implementation
 */
public final class DefaultConfigurationResource extends BaseResource implements ConfigurationResource {
    
    private final ConfigId id;
    private final ConfigType type;
    private final String path;
    private final ConfigSource source;
    private final Map<String, Object> values;
    private final Instant loadedAt;
    private final Instant lastModifiedAt;
    private final ConfigStatus status;
    private final Map<String, ConfigChange> changeHistory;
    
    private DefaultConfigurationResource(Builder builder) {
        super(
            builder.id != null ? builder.id : new ConfigId(Id.random()),
            builder.metadata != null ? builder.metadata : Metadata.builder()
                .name(builder.name != null ? builder.name : "config-" + builder.path)
                .description("Configuration resource")
                .version(builder.version != null ? builder.version : Version.VERSION_1_0_0)
                .label("type", builder.type.name().toLowerCase())
                .label("source", builder.source != null ? builder.source.name() : "unknown")
                .now()
                .build()
        );
        this.id = (ConfigId) super.id();
        this.type = builder.type;
        this.path = builder.path;
        this.source = builder.source;
        this.values = builder.values != null ? new LinkedHashMap<>(builder.values) : new LinkedHashMap<>();
        this.loadedAt = builder.loadedAt != null ? builder.loadedAt : Instant.now();
        this.lastModifiedAt = builder.lastModifiedAt != null ? builder.lastModifiedAt : this.loadedAt;
        this.status = builder.status != null ? builder.status : ConfigStatus.LOADED;
        this.changeHistory = builder.changeHistory != null ? builder.changeHistory : new LinkedHashMap<>();
    }
    
    // === ConfigurationResource Implementation ===
    
    @Override
    public ConfigId id() {
        return id;
    }
    
    @Override
    public ConfigType configType() {
        return type;
    }
    
    @Override
    public String path() {
        return path;
    }
    
    @Override
    public Object value() {
        return values;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <T> T valueAs(Class<T> type) {
        if (type.isInstance(values)) {
            return type.cast(values);
        }
        // Try to convert
        return null;
    }
    
    @Override
    public ConfigSource source() {
        return source;
    }
    
    @Override
    public ConfigStatus status() {
        return status;
    }
    
    @Override
    public Instant loadedAt() {
        return loadedAt;
    }
    
    @Override
    public Instant lastModifiedAt() {
        return lastModifiedAt;
    }
    
    @Override
    public Map<String, Object> allValues() {
        return new LinkedHashMap<>(values);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ConfigurationResource getSection(String path) {
        Object section = get(path, Object.class);
        if (section instanceof Map) {
            return new DefaultConfigurationResource.Builder()
                .id(new ConfigId(Id.random()))
                .type(ConfigType.SECTION)
                .path(this.path + "." + path)
                .source(source)
                .values((Map<String, Object>) section)
                .loadedAt(loadedAt)
                .lastModifiedAt(lastModifiedAt)
                .status(status)
                .metadata(Metadata.builder()
                    .name("section-" + path)
                    .description("Configuration section: " + path)
                    .version(metadata().version())
                    .label("parent", this.path)
                    .now()
                    .build())
                .build();
        }
        return new DefaultConfigurationResource.Builder()
            .id(new ConfigId(Id.random()))
            .type(ConfigType.SECTION)
            .path(this.path + "." + path)
            .source(source)
            .values(Map.of())
            .loadedAt(loadedAt)
            .lastModifiedAt(lastModifiedAt)
            .status(ConfigStatus.EMPTY)
            .build();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public boolean has(String path) {
        return get(path, Object.class) != null;
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
        
        if (type.isInstance(current)) {
            return type.cast(current);
        }
        
        // Type conversion
        return convertValue(current, type);
    }
    
    @SuppressWarnings("unchecked")
    private <T> T convertValue(Object current, Class<T> type) {
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
            if (current instanceof String) {
                return (T) Boolean.valueOf((String) current);
            }
        }
        if (type == String.class) {
            return (T) current.toString();
        }
        if (type == List.class) {
            if (current instanceof List) {
                return (T) current;
            }
        }
        if (type == Map.class) {
            if (current instanceof Map) {
                return (T) current;
            }
        }
        return null;
    }
    
    @Override
    public <T> T get(String path, Class<T> type, T defaultValue) {
        T value = get(path, type);
        return value != null ? value : defaultValue;
    }
    
    @Override
    public ConfigurationResource merge(ConfigurationResource other) {
        Map<String, Object> merged = new LinkedHashMap<>(values);
        if (other != null) {
            deepMerge(merged, other.allValues());
        }
        return new DefaultConfigurationResource.Builder()
            .id(new ConfigId(Id.random()))
            .type(ConfigType.MERGED)
            .path(path + ".merged")
            .source(source)
            .values(merged)
            .loadedAt(Instant.now())
            .lastModifiedAt(Instant.now())
            .status(ConfigStatus.LOADED)
            .metadata(Metadata.builder()
                .name("merged-config-" + path)
                .description("Merged configuration")
                .version(metadata().version())
                .label("source1", id().asString())
                .label("source2", other != null ? other.id().asString() : "none")
                .now()
                .build())
            .build();
    }
    
    @Override
    public ConfigurationResource withValue(String path, Object value) {
        Map<String, Object> newValues = new LinkedHashMap<>(values);
        setValue(newValues, path, value);
        return new DefaultConfigurationResource.Builder()
            .id(new ConfigId(Id.random()))
            .type(ConfigType.RUNTIME)
            .path(this.path)
            .source(ConfigSource.RUNTIME)
            .values(newValues)
            .loadedAt(loadedAt)
            .lastModifiedAt(Instant.now())
            .status(ConfigStatus.MODIFIED)
            .metadata(Metadata.builder()
                .name(metadata().name())
                .description(metadata().description())
                .version(metadata().version())
                .label("modified", "true")
                .label("parent", id().asString())
                .now()
                .build())
            .build();
    }
    
    @Override
    public ConfigurationResource withStatus(ConfigStatus status) {
        return new DefaultConfigurationResource.Builder()
            .id(id)
            .type(type)
            .path(path)
            .source(source)
            .values(values)
            .loadedAt(loadedAt)
            .lastModifiedAt(lastModifiedAt)
            .status(status)
            .metadata(metadata())
            .changeHistory(changeHistory)
            .build();
    }
    
    @Override
    public ConfigurationResource reload() throws Exception {
        if (source == null || source == ConfigSource.RUNTIME || source == ConfigSource.MEMORY) {
            return this;
        }
        
        // Reload from source
        throw new UnsupportedOperationException("reload() not supported on generic resources");
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
    
    @SuppressWarnings("unchecked")
    private void setValue(Map<String, Object> target, String path, Object value) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = target;
        
        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            current = (Map<String, Object>) current.computeIfAbsent(part, k -> new LinkedHashMap<>());
        }
        
        current.put(parts[parts.length - 1], value);
    }
    
    // === Resource Overrides ===
    
    @Override
    public ResourceType type() {
        return new ResourceType.Configuration();
    }
    

    
    // === Builder ===
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private ConfigId id;
        private ConfigType type;
        private String path;
        private ConfigSource source;
        private Map<String, Object> values;
        private Instant loadedAt;
        private Instant lastModifiedAt;
        private ConfigStatus status;
        private Metadata metadata;
        private String name;
        private Version version;
        private Map<String, ConfigChange> changeHistory;
        
        public Builder id(ConfigId id) {
            this.id = id;
            return this;
        }
        
        public Builder type(ConfigType type) {
            this.type = type;
            return this;
        }
        
        public Builder path(String path) {
            this.path = path;
            return this;
        }
        
        public Builder source(ConfigSource source) {
            this.source = source;
            return this;
        }
        
        public Builder values(Map<String, Object> values) {
            this.values = values;
            return this;
        }
        
        public Builder value(String key, Object value) {
            if (values == null) {
                values = new LinkedHashMap<>();
            }
            values.put(key, value);
            return this;
        }
        
        public Builder loadedAt(Instant loadedAt) {
            this.loadedAt = loadedAt;
            return this;
        }
        
        public Builder lastModifiedAt(Instant lastModifiedAt) {
            this.lastModifiedAt = lastModifiedAt;
            return this;
        }
        
        public Builder status(ConfigStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder version(Version version) {
            this.version = version;
            return this;
        }
        
        public Builder changeHistory(Map<String, ConfigChange> changeHistory) {
            this.changeHistory = changeHistory;
            return this;
        }
        
        public DefaultConfigurationResource build() {
            if (id == null) {
                id = new ConfigId(Id.random());
            }
            if (type == null) {
                type = ConfigType.UNKNOWN;
            }
            if (values == null) {
                values = new LinkedHashMap<>();
            }
            if (loadedAt == null) {
                loadedAt = Instant.now();
            }
            if (lastModifiedAt == null) {
                lastModifiedAt = loadedAt;
            }
            if (status == null) {
                status = ConfigStatus.LOADED;
            }
            if (changeHistory == null) {
                changeHistory = new LinkedHashMap<>();
            }
            return new DefaultConfigurationResource(this);
        }
    }
}
