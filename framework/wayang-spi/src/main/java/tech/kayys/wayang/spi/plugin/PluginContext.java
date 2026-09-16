package tech.kayys.wayang.spi.plugin;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

/**
 * Runtime context supplied to a Wayang plugin during initialization.
 *
 * <p>The context represents application/plugin scope. It must not
 * contain tenant, user, or execution-specific state.</p>
 */
public interface PluginContext {

    /**
     * Plugin identifier.
     */
    String pluginId();

    /**
     * Plugin manifest.
     */
    Manifest manifest();

    /**
     * Classloader associated with the plugin.
     */
    ClassLoader classLoader();

    /**
     * Application-level service available to the plugin.
     *
     * <p>The concrete configuration or registry implementation is intentionally
     * exposed through the service mechanism so that wayang-spi does
     * not become coupled to those modules.</p>
     */
    <T> Optional<T> service(Class<T> type);

    /**
     * Plugin-specific data directory.
     */
    Optional<Path> dataDirectory();

    /**
     * Read-only plugin attributes.
     */
    Map<String, Object> attributes();

    /**
     * Convenience lookup for a plugin attribute.
     */
    default Optional<Object> attribute(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(attributes().get(name));
    }
}
