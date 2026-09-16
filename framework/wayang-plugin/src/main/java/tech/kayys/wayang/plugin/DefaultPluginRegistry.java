package tech.kayys.wayang.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginRegistry;
import tech.kayys.wayang.spi.plugin.PluginState;

/**
 * Thread-safe in-memory implementation of the Wayang PluginRegistry.
 *
 * <p>The registry is responsible only for registration and lookup.
 * Loading, dependency resolution, lifecycle management, and persistence
 * are handled by their respective runtime components.</p>
 */
public final class DefaultPluginRegistry implements PluginRegistry {

    private final ConcurrentMap<String, Plugin> plugins =
            new ConcurrentHashMap<>();

    /**
     * Registers a plugin.
     *
     * @throws NullPointerException if the plugin or plugin id is null
     * @throws IllegalStateException if another plugin already owns the id
     */
    public void register(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin cannot be null");

        String id = Objects.requireNonNull(
                plugin.id(),
                "plugin.id() cannot be null"
        );

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "plugin.id() cannot be blank"
            );
        }

        Plugin existing = plugins.putIfAbsent(id, plugin);

        if (existing != null) {
            throw new IllegalStateException(
                    "Plugin already registered: " + id
            );
        }
    }

    /**
     * Replaces an existing plugin with the supplied plugin.
     *
     * <p>This operation is intended for controlled reload/update
     * scenarios and should not normally be used during startup.</p>
     */
    public void replace(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin cannot be null");

        String id = Objects.requireNonNull(
                plugin.id(),
                "plugin.id() cannot be null"
        );

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "plugin.id() cannot be blank"
            );
        }

        plugins.put(id, plugin);
    }

    /**
     * Unregisters a plugin.
     *
     * @return the removed plugin, if present
     */
    @Override
    public Optional<Plugin> unregister(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(plugins.remove(id));
    }

    /**
     * Tests whether a plugin is registered.
     */
    public boolean contains(String id) {
        return id != null && plugins.containsKey(id);
    }

    @Override
    public Optional<Plugin> getPlugin(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(plugins.get(id));
    }

    @Override
    public List<Plugin> getPlugins() {
        return List.copyOf(plugins.values());
    }

    @Override
    public List<Extension> getExtensions(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            return List.of();
        }

        Plugin plugin = plugins.get(pluginId);

        if (plugin == null) {
            return List.of();
        }

        List<Extension> extensions = plugin.extensions();

        if (extensions == null || extensions.isEmpty()) {
            return List.of();
        }

        return List.copyOf(extensions);
    }

    @Override
    public <T extends Extension> List<T> getExtensions(Class<T> type) {
        Objects.requireNonNull(type, "type cannot be null");

        List<T> result = new ArrayList<>();

        for (Plugin plugin : plugins.values()) {
            List<Extension> extensions = plugin.extensions();

            if (extensions == null || extensions.isEmpty()) {
                continue;
            }

            for (Extension extension : extensions) {
                if (type.isInstance(extension)) {
                    result.add(type.cast(extension));
                }
            }
        }

        return List.copyOf(result);
    }

    /**
     * Filter registered plugins by lifecycle state.
     */
    public List<Plugin> getPluginsByState(PluginState state) {
        Objects.requireNonNull(state, "state cannot be null");

        return plugins.values()
                .stream()
                .filter(plugin -> plugin.state() == state)
                .toList();
    }

    /**
     * Filter registered plugins by provided capability.
     */
    public List<Plugin> getPluginsProvidingCapability(String capability) {
        if (capability == null || capability.isBlank()) {
            return List.of();
        }

        return plugins.values()
                .stream()
                .filter(plugin -> plugin.manifest() != null
                        && plugin.manifest().provides(capability))
                .toList();
    }
}
