package tech.kayys.wayang.plugin.dto;

public class PluginContext {
    private final String pluginId;
    private final SemanticVersion version;
    private final Map<String, Object> config;
    private final SecurityContext securityContext;
    private final PluginMetrics metrics;
    private final EventEmitter eventEmitter;

    public PluginContext(
            String pluginId,
            SemanticVersion version,
            Map<String, Object> config,
            SecurityContext securityContext,
            PluginMetrics metrics) {
        this.pluginId = pluginId;
        this.version = version;
        this.config = Collections.unmodifiableMap(config);
        this.securityContext = securityContext;
        this.metrics = metrics;
        this.eventEmitter = new PluginEventEmitter(pluginId);
    }

    public String getPluginId() {
        return pluginId;
    }

    public SemanticVersion getVersion() {
        return version;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public SecurityContext getSecurityContext() {
        return securityContext;
    }

    public PluginMetrics getMetrics() {
        return metrics;
    }

    public EventEmitter getEventEmitter() {
        return eventEmitter;
    }

    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key, Class<T> type) {
        Object value = config.get(key);
        if (value == null)
            return null;
        if (type.isInstance(value)) {
            return (T) value;
        }
        throw new ConfigTypeMismatchException(key, type, value.getClass());
    }

    public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        T value = getConfigValue(key, type);
        return value != null ? value : defaultValue;
    }
}
