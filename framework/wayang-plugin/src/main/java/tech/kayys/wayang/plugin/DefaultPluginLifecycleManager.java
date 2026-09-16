package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.Dependency;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginContext;
import tech.kayys.wayang.spi.plugin.PluginDependencyResolver;
import tech.kayys.wayang.spi.plugin.PluginLifecycleManager;
import tech.kayys.wayang.spi.plugin.PluginRegistry;
import tech.kayys.wayang.spi.plugin.PluginState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Default application-scoped plugin lifecycle manager.
 *
 * <p>Owns lifecycle coordination, dependency-ordered activation,
 * reverse-order shutdown, and state tracking for registered plugins.</p>
 */
public final class DefaultPluginLifecycleManager implements PluginLifecycleManager {

    private final PluginRegistry pluginRegistry;
    private final Function<Plugin, PluginContext> contextFactory;
    private final PluginDependencyResolver dependencyResolver;

    private final ConcurrentMap<String, PluginState> states =
            new ConcurrentHashMap<>();

    private final List<String> startupOrder =
            new ArrayList<>();

    private final ReentrantLock lifecycleLock =
            new ReentrantLock();

    public DefaultPluginLifecycleManager(
            PluginRegistry pluginRegistry,
            Function<Plugin, PluginContext> contextFactory,
            PluginDependencyResolver dependencyResolver) {

        this.pluginRegistry = Objects.requireNonNull(
                pluginRegistry,
                "pluginRegistry cannot be null");

        this.contextFactory = Objects.requireNonNull(
                contextFactory,
                "contextFactory cannot be null");

        this.dependencyResolver = Objects.requireNonNull(
                dependencyResolver,
                "dependencyResolver cannot be null");

        synchronizeRegistryState();
    }

    public DefaultPluginLifecycleManager(PluginRegistry pluginRegistry) {
        this(
                pluginRegistry,
                plugin -> {
                    Manifest manifest = plugin.manifest();
                    if (manifest == null) {
                        manifest = DefaultManifest.builder()
                                .id(tech.kayys.wayang.spi.plugin.ManifestId.random())
                                .name(plugin.id())
                                .build();
                    }
                    return PluginContextBuilder.create()
                            .pluginId(plugin.id())
                            .manifest(manifest)
                            .classLoader(plugin.classLoader() != null ? plugin.classLoader() : plugin.getClass().getClassLoader())
                            .build();
                },
                new DefaultPluginDependencyResolver()
        );
    }

    @Override
    public void enablePlugin(String id) throws Exception {
        requirePluginId(id);

        lifecycleLock.lock();
        try {
            Plugin plugin = requirePlugin(id);
            enableResolved(plugin);
        } finally {
            lifecycleLock.unlock();
        }
    }

    private void enableResolved(Plugin plugin) throws Exception {
        String id = plugin.id();
        PluginState state = stateOf(id);

        if (state == PluginState.ACTIVE) {
            return;
        }

        if (state == PluginState.STARTING) {
            throw new PluginLifecycleException("Plugin is already starting: " + id);
        }

        if (state == PluginState.STOPPING) {
            throw new PluginLifecycleException("Plugin is stopping: " + id);
        }

        if (state == PluginState.ERROR) {
            throw new PluginLifecycleException(
                    "Plugin is in ERROR state and must be explicitly recovered before enabling: " + id);
        }

        List<Plugin> all = pluginRegistry.getPlugins();
        List<Plugin> ordered = dependencyResolver.resolve(all);

        for (Plugin candidate : ordered) {
            if (!requiresActivation(candidate, plugin)) {
                continue;
            }

            enableSingle(candidate);
        }
    }

    private boolean requiresActivation(Plugin candidate, Plugin target) {
        if (candidate.id().equals(target.id())) {
            return true;
        }

        return dependsTransitivelyOn(target, candidate.id(), new HashSet<>());
    }

    private boolean dependsTransitivelyOn(
            Plugin plugin,
            String targetId,
            Set<String> visited) {

        if (!visited.add(plugin.id())) {
            return false;
        }

        if (plugin.manifest() == null) {
            return false;
        }

        List<Dependency> dependencies = plugin.manifest().dependencies();

        if (dependencies == null) {
            return false;
        }

        for (Dependency dependency : dependencies) {
            if (dependency == null || dependency.isProvided()) {
                continue;
            }

            if (dependency.id().equals(targetId)) {
                return true;
            }

            try {
                Optional<Plugin> depPlugin = pluginRegistry.getPlugin(dependency.id());
                if (depPlugin.isPresent() && dependsTransitivelyOn(depPlugin.get(), targetId, visited)) {
                    return true;
                }
            } catch (Exception ignored) {
                // fall through
            }
        }

        return false;
    }

    private void enableSingle(Plugin plugin) throws Exception {
        String id = plugin.id();
        PluginState state = stateOf(id);

        if (state == PluginState.ACTIVE) {
            return;
        }

        if (state == PluginState.STARTING) {
            throw new PluginLifecycleException("Plugin is already starting: " + id);
        }

        transition(id, PluginState.RESOLVED);
        transition(id, PluginState.STARTING);

        try {
            PluginContext context = contextFactory.apply(plugin);
            if (context == null) {
                throw new IllegalStateException("Plugin context factory returned null: " + id);
            }

            plugin.initialize(context);
            plugin.start();

            transition(id, PluginState.ACTIVE);

            if (!startupOrder.contains(id)) {
                startupOrder.add(id);
            }

        } catch (Exception e) {
            transition(id, PluginState.ERROR);
            throw new PluginLifecycleException("Failed to start plugin: " + id, e);
        }
    }

    @Override
    public void disablePlugin(String id) throws Exception {
        requirePluginId(id);

        lifecycleLock.lock();
        try {
            Plugin plugin = requirePlugin(id);
            disableDependents(plugin);
            disableSingle(plugin);
        } finally {
            lifecycleLock.unlock();
        }
    }

    private void disableDependents(Plugin dependency) throws Exception {
        List<Plugin> plugins = pluginRegistry.getPlugins();

        for (Plugin plugin : plugins) {
            if (plugin.id().equals(dependency.id())) {
                continue;
            }

            if (!isActive(plugin.id())) {
                continue;
            }

            if (dependsTransitivelyOn(plugin, dependency.id(), new HashSet<>())) {
                disableDependents(plugin);
                disableSingle(plugin);
            }
        }
    }

    private void disableSingle(Plugin plugin) throws Exception {
        String id = plugin.id();
        PluginState state = stateOf(id);

        if (state == PluginState.STOPPED
                || state == PluginState.LOADED
                || state == PluginState.RESOLVED) {
            return;
        }

        if (state == PluginState.ERROR) {
            transition(id, PluginState.STOPPED);
            startupOrder.remove(id);
            return;
        }

        if (state != PluginState.ACTIVE && state != PluginState.STARTING) {
            return;
        }

        transition(id, PluginState.STOPPING);

        try {
            plugin.stop();
            transition(id, PluginState.STOPPED);
            startupOrder.remove(id);
        } catch (Exception e) {
            transition(id, PluginState.ERROR);
            throw new PluginLifecycleException("Failed to stop plugin: " + id, e);
        }
    }

    @Override
    public void unloadPlugin(String id) throws Exception {
        requirePluginId(id);

        lifecycleLock.lock();
        try {
            Plugin plugin = requirePlugin(id);

            disableDependents(plugin);
            disableSingle(plugin);

            if (stateOf(id) == PluginState.ACTIVE || stateOf(id) == PluginState.STOPPING) {
                throw new PluginLifecycleException("Plugin cannot be unloaded while active: " + id);
            }

            pluginRegistry.unregister(id);

            states.remove(id);
            startupOrder.remove(id);

        } finally {
            lifecycleLock.unlock();
        }
    }

    public void enableAll() throws Exception {
        lifecycleLock.lock();
        try {
            List<Plugin> ordered = dependencyResolver.resolve(pluginRegistry.getPlugins());

            for (Plugin plugin : ordered) {
                transition(plugin.id(), PluginState.RESOLVED);
            }

            for (Plugin plugin : ordered) {
                enableSingle(plugin);
            }
        } finally {
            lifecycleLock.unlock();
        }
    }

    public void disableAll() throws Exception {
        lifecycleLock.lock();
        try {
            List<String> order = List.copyOf(startupOrder);
            Exception firstFailure = null;

            for (int i = order.size() - 1; i >= 0; i--) {
                String id = order.get(i);
                Optional<Plugin> plugin = pluginRegistry.getPlugin(id);

                if (plugin.isPresent()) {
                    try {
                        disableSingle(plugin.get());
                    } catch (Exception e) {
                        if (firstFailure == null) {
                            firstFailure = e;
                        }
                    }
                }
            }

            if (firstFailure != null) {
                throw firstFailure;
            }
        } finally {
            lifecycleLock.unlock();
        }
    }

    public List<String> getStartupOrder() {
        lifecycleLock.lock();
        try {
            return List.copyOf(startupOrder);
        } finally {
            lifecycleLock.unlock();
        }
    }

    @Override
    public PluginState getPluginState(String id) {
        if (id == null || id.isBlank()) {
            return PluginState.ERROR;
        }

        return stateOf(id);
    }

    private Plugin requirePlugin(String id) throws PluginLifecycleException {
        if (id == null || id.isBlank()) {
            throw new PluginLifecycleException("Plugin ID cannot be null or blank");
        }

        try {
            return pluginRegistry.getPlugin(id).orElseThrow(() ->
                    new PluginLifecycleException("Plugin not found: " + id));
        } catch (PluginLifecycleException ple) {
            throw ple;
        } catch (Exception e) {
            throw new PluginLifecycleException("Error querying plugin: " + id, e);
        }
    }

    private PluginState stateOf(String id) {
        PluginState state = states.get(id);
        if (state != null) {
            return state;
        }
        return PluginState.LOADED;
    }

    private boolean isActive(String id) {
        return stateOf(id) == PluginState.ACTIVE;
    }

    private void transition(String id, PluginState state) {
        states.put(id, state);
    }

    private void synchronizeRegistryState() {
        try {
            for (Plugin plugin : pluginRegistry.getPlugins()) {
                if (plugin != null && plugin.id() != null) {
                    states.putIfAbsent(plugin.id(), PluginState.LOADED);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static String requirePluginId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Plugin ID cannot be null or blank");
        }
        return id;
    }
}
