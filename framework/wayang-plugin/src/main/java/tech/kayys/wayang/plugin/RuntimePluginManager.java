package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginLifecycleManager;
import tech.kayys.wayang.spi.plugin.PluginRegistry;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * High-level orchestration for runtime plugin installation, activation,
 * deactivation, uninstallation, and resource cleanup.
 */
public final class RuntimePluginManager {

    private final RuntimePluginLoader loader;
    private final PluginRegistry registry;
    private final PluginLifecycleManager lifecycleManager;

    private final ConcurrentMap<String, RuntimePluginHandle> handles =
            new ConcurrentHashMap<>();

    public RuntimePluginManager(
            RuntimePluginLoader loader,
            PluginRegistry registry,
            PluginLifecycleManager lifecycleManager) {

        this.loader = Objects.requireNonNull(loader, "loader cannot be null");
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.lifecycleManager = Objects.requireNonNull(lifecycleManager, "lifecycleManager cannot be null");
    }

    public Plugin install(Path jar) throws Exception {
        RuntimePluginHandle handle = loader.load(jar);
        Plugin plugin = handle.plugin();

        RuntimePluginHandle existing = handles.putIfAbsent(plugin.id(), handle);
        if (existing != null) {
            handle.close();
            throw new IllegalStateException("Runtime plugin already installed: " + plugin.id());
        }

        try {
            if (registry instanceof DefaultPluginRegistry defaultRegistry) {
                defaultRegistry.register(plugin);
            }
            return plugin;
        } catch (Exception e) {
            handles.remove(plugin.id(), handle);
            handle.close();
            throw e;
        }
    }

    public void enable(String pluginId) throws Exception {
        requireHandle(pluginId);
        lifecycleManager.enablePlugin(pluginId);
    }

    public void disable(String pluginId) throws Exception {
        requireHandle(pluginId);
        lifecycleManager.disablePlugin(pluginId);
    }

    public void uninstall(String pluginId) throws Exception {
        RuntimePluginHandle handle = requireHandle(pluginId);

        try {
            lifecycleManager.disablePlugin(pluginId);
        } catch (Exception ignored) {
            // best-effort stop before removal
        }

        registry.unregister(pluginId);
        handles.remove(pluginId, handle);
        handle.close();
    }

    public Optional<RuntimePluginHandle> handle(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(handles.get(pluginId));
    }

    private RuntimePluginHandle requireHandle(String pluginId) {
        if (pluginId == null || pluginId.isBlank()) {
            throw new IllegalArgumentException("pluginId cannot be null or blank");
        }

        RuntimePluginHandle handle = handles.get(pluginId);
        if (handle == null) {
            throw new IllegalStateException("Runtime plugin is not installed: " + pluginId);
        }

        return handle;
    }
}
