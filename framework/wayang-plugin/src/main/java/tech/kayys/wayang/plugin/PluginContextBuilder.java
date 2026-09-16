package tech.kayys.wayang.plugin;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.ManifestId;
import tech.kayys.wayang.spi.plugin.PluginContext;

/**
 * Builder for immutable PluginContext instances.
 */
public final class PluginContextBuilder {

    private String pluginId;
    private Manifest manifest;
    private ClassLoader classLoader;
    private Path dataDirectory;

    private final Map<Class<?>, Object> services = new LinkedHashMap<>();
    private final Map<String, Object> attributes = new LinkedHashMap<>();

    public static PluginContextBuilder create() {
        return new PluginContextBuilder();
    }

    public PluginContextBuilder pluginId(String pluginId) {
        this.pluginId = pluginId;
        return this;
    }

    public PluginContextBuilder manifest(Manifest manifest) {
        this.manifest = manifest;
        return this;
    }

    public PluginContextBuilder classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public PluginContextBuilder dataDirectory(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        return this;
    }

    public <T> PluginContextBuilder service(Class<T> type, T service) {
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(service, "service cannot be null");

        services.put(type, service);
        return this;
    }

    public PluginContextBuilder attribute(String name, Object value) {
        Objects.requireNonNull(name, "name cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("attribute name cannot be blank");
        }

        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }

        return this;
    }

    public PluginContext build() {
        Objects.requireNonNull(pluginId, "pluginId is required");
        Objects.requireNonNull(classLoader, "classLoader is required");

        if (manifest == null) {
            manifest = DefaultManifest.builder()
                    .id(ManifestId.random())
                    .name(pluginId)
                    .build();
        }

        return new DefaultPluginContext(
                pluginId,
                manifest,
                classLoader,
                services,
                dataDirectory,
                attributes
        );
    }
}
