package tech.kayys.wayang.configuration;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.extension.Version;

/**
 * Configuration Registry - manages configurations as resources
 */
public class ConfigurationRegistry {
    
    private final Map<ConfigId, ConfigurationResource> configurations = new ConcurrentHashMap<>();
    private final Map<String, ConfigId> pathIndex = new ConcurrentHashMap<>();
    private final Map<ConfigSource, Set<ConfigId>> sourceIndex = new ConcurrentHashMap<>();
    private ConfigurationResource activeConfig;
    
    /**
     * Register a configuration resource
     */
    public void register(ConfigurationResource config) {
        configurations.put(config.id(), config);
        pathIndex.put(config.path(), config.id());
        sourceIndex.computeIfAbsent(config.source(), k -> ConcurrentHashMap.newKeySet()).add(config.id());
        
        // If this is an application config, set as active
        if (config.configType() == ConfigType.APPLICATION) {
            activeConfig = config;
        }
    }
    
    /**
     * Get configuration by ID
     */
    public Optional<ConfigurationResource> get(ConfigId id) {
        return Optional.ofNullable(configurations.get(id));
    }
    
    /**
     * Get configuration by path
     */
    public Optional<ConfigurationResource> getByPath(String path) {
        ConfigId id = pathIndex.get(path);
        return id != null ? Optional.ofNullable(configurations.get(id)) : Optional.empty();
    }
    
    /**
     * Get active configuration
     */
    public ConfigurationResource getActive() {
        if (activeConfig == null) {
            // Load default
            activeConfig = loadDefaultConfig();
        }
        return activeConfig;
    }
    
    /**
     * Set active configuration
     */
    public void setActive(ConfigId id) {
        ConfigurationResource config = configurations.get(id);
        if (config != null) {
            this.activeConfig = config;
        }
    }
    
    /**
     * List all configurations
     */
    public List<ConfigurationResource> list() {
        return new ArrayList<>(configurations.values());
    }
    
    /**
     * List configurations by source
     */
    public List<ConfigurationResource> listBySource(ConfigSource source) {
        Set<ConfigId> ids = sourceIndex.getOrDefault(source, Set.of());
        return ids.stream()
            .map(configurations::get)
            .filter(Objects::nonNull)
            .toList();
    }
    
    /**
     * Load configuration from file
     */
    public ConfigurationResource loadFromFile(Path path) throws Exception {
        Map<String, Object> values = loadFile(path);
        ConfigurationResource config = DefaultConfigurationResource.builder()
            .path(path.toString())
            .source(ConfigSource.FILE)
            .type(ConfigType.FILE)
            .values(values)
            .name("file-config-" + path.getFileName())
            .version(Version.VERSION_1_0_0)
            .status(ConfigStatus.LOADED)
            .build();
        register(config);
        return config;
    }
    
    /**
     * Load configuration from environment
     */
    public ConfigurationResource loadFromEnvironment(String prefix) {
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            if (entry.getKey().startsWith(prefix)) {
                String path = entry.getKey().substring(prefix.length())
                    .toLowerCase()
                    .replace('_', '.');
                values.put(path, entry.getValue());
            }
        }
        ConfigurationResource config = DefaultConfigurationResource.builder()
            .path("environment")
            .source(ConfigSource.ENVIRONMENT)
            .type(ConfigType.ENVIRONMENT)
            .values(values)
            .name("env-config")
            .version(Version.VERSION_1_0_0)
            .status(ConfigStatus.LOADED)
            .build();
        register(config);
        return config;
    }
    
    /**
     * Create a merged configuration from multiple sources
     */
    public ConfigurationResource merge(ConfigurationResource... configs) {
        Map<String, Object> merged = new LinkedHashMap<>();
        for (ConfigurationResource config : configs) {
            deepMerge(merged, config.allValues());
        }
        
        ConfigurationResource config = DefaultConfigurationResource.builder()
            .path("merged")
            .source(ConfigSource.MERGED)
            .type(ConfigType.MERGED)
            .values(merged)
            .name("merged-config")
            .version(Version.VERSION_1_0_0)
            .status(ConfigStatus.LOADED)
            .build();
        register(config);
        return config;
    }
    
    /**
     * Create a tenant-specific configuration
     */
    public ConfigurationResource createTenantConfig(String tenantId, ConfigurationResource baseConfig) {
        Map<String, Object> values = new LinkedHashMap<>(baseConfig.allValues());
        // Override with tenant-specific values
        // In practice, this would load from tenant-specific file or database
        
        ConfigurationResource config = DefaultConfigurationResource.builder()
            .path("tenant/" + tenantId)
            .source(ConfigSource.TENANT)
            .type(ConfigType.TENANT)
            .values(values)
            .name("tenant-config-" + tenantId)
            .version(Version.VERSION_1_0_0)
            .status(ConfigStatus.LOADED)
            .metadata(Metadata.builder()
                .name("tenant-config-" + tenantId)
                .description("Configuration for tenant: " + tenantId)
                .label("tenant", tenantId)
                .now()
                .build())
            .build();
        register(config);
        return config;
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
    private Map<String, Object> loadFile(Path path) throws Exception {
        if (!Files.exists(path)) {
            return new LinkedHashMap<>();
        }
        
        String content = Files.readString(path);
        String fileName = path.getFileName().toString().toLowerCase();
        
        if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = 
                new com.fasterxml.jackson.databind.ObjectMapper(new com.fasterxml.jackson.dataformat.yaml.YAMLFactory());
            return mapper.readValue(content, Map.class);
        } else if (fileName.endsWith(".json")) {
            com.fasterxml.jackson.databind.ObjectMapper mapper = 
                new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(content, Map.class);
        } else if (fileName.endsWith(".properties")) {
            Properties props = new Properties();
            props.load(new java.io.StringReader(content));
            Map<String, Object> result = new LinkedHashMap<>();
            for (String key : props.stringPropertyNames()) {
                result.put(key, props.getProperty(key));
            }
            return result;
        }
        
        return new LinkedHashMap<>();
    }
    
    private ConfigurationResource loadDefaultConfig() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("wayang.runtime.maxThreads", 200);
        defaults.put("wayang.runtime.defaultTimeout", 30000);
        defaults.put("wayang.memory.defaultTtl", 3600);
        defaults.put("wayang.circuitBreaker.failureThreshold", 5);
        defaults.put("wayang.circuitBreaker.timeout", 30000);
        defaults.put("wayang.retry.maxAttempts", 3);
        defaults.put("wayang.retry.initialDelay", 1000);
        
        ConfigurationResource config = DefaultConfigurationResource.builder()
            .path("default")
            .source(ConfigSource.DEFAULT)
            .type(ConfigType.APPLICATION)
            .values(defaults)
            .name("default-config")
            .version(Version.VERSION_1_0_0)
            .status(ConfigStatus.LOADED)
            .build();
        register(config);
        return config;
    }
}