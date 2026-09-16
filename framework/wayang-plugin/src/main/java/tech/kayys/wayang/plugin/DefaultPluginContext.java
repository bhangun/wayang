package tech.kayys.wayang.plugin;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.PluginContext;

/**
 * Default immutable implementation of PluginContext.
 */
public final class DefaultPluginContext implements PluginContext {

    private final String pluginId;
    private final Manifest manifest;
    private final ClassLoader classLoader;
    private final Map<Class<?>, Object> services;
    private final Path dataDirectory;
    private final Map<String, Object> attributes;

    public DefaultPluginContext(
            String pluginId,
            Manifest manifest,
            ClassLoader classLoader,
            Map<Class<?>, Object> services,
            Path dataDirectory,
            Map<String, Object> attributes) {

        this.pluginId = Objects.requireNonNull(pluginId, "pluginId cannot be null");
        this.manifest = Objects.requireNonNull(manifest, "manifest cannot be null");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader cannot be null");

        this.services = services == null
                ? Map.of()
                : Map.copyOf(services);

        this.dataDirectory = dataDirectory;

        this.attributes = attributes == null
                ? Map.of()
                : Map.copyOf(attributes);
    }

    @Override
    public String pluginId() {
        return pluginId;
    }

    @Override
    public Manifest manifest() {
        return manifest;
    }

    @Override
    public ClassLoader classLoader() {
        return classLoader;
    }

    @Override
    public <T> Optional<T> service(Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null");

        Object service = services.get(type);

        if (service == null) {
            return Optional.empty();
        }

        return Optional.of(type.cast(service));
    }

    @Override
    public Optional<Path> dataDirectory() {
        return Optional.ofNullable(dataDirectory);
    }

    @Override
    public Map<String, Object> attributes() {
        return attributes;
    }
}
