package tech.kayys.wayang.plugin;

import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.spi.plugin.Dependency;
import tech.kayys.wayang.spi.plugin.DependencyScope;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginDependencyResolver;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Default implementation of PluginDependencyResolver enforcing topological sorting,
 * cycle detection, and dependency scope semantics.
 */
public final class DefaultPluginDependencyResolver implements PluginDependencyResolver {

    @Override
    public List<Plugin> resolve(List<Plugin> plugins) throws Exception {
        Objects.requireNonNull(plugins, "plugins cannot be null");

        Map<String, Plugin> byId = indexPlugins(plugins);

        validateUniqueIds(byId, plugins);

        Map<String, Set<String>> graph = buildDependencyGraph(byId);

        detectCycles(graph);

        return topologicalSort(byId, graph);
    }

    private Map<String, Plugin> indexPlugins(List<Plugin> plugins) throws PluginDependencyException {
        Map<String, Plugin> result = new LinkedHashMap<>();

        for (Plugin plugin : plugins) {
            if (plugin == null) {
                throw new IllegalArgumentException("Plugin list contains null");
            }

            String id = plugin.id();

            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Plugin has null or blank id");
            }

            Plugin previous = result.putIfAbsent(id, plugin);

            if (previous != null) {
                throw new PluginDependencyException("Duplicate plugin id: " + id);
            }
        }

        return result;
    }

    private void validateUniqueIds(Map<String, Plugin> byId, List<Plugin> plugins) {
        if (byId.size() != plugins.size()) {
            throw new IllegalStateException("Plugin index size differs from plugin list");
        }
    }

    private Map<String, Set<String>> buildDependencyGraph(Map<String, Plugin> plugins)
            throws PluginDependencyException {

        Map<String, Set<String>> graph = new LinkedHashMap<>();

        for (Plugin plugin : plugins.values()) {
            graph.put(plugin.id(), new LinkedHashSet<>());
        }

        for (Plugin plugin : plugins.values()) {
            if (plugin.manifest() == null) {
                continue;
            }

            List<Dependency> dependencies = plugin.manifest().dependencies();

            if (dependencies == null || dependencies.isEmpty()) {
                continue;
            }

            for (Dependency dependency : dependencies) {
                if (dependency == null) {
                    throw new PluginDependencyException(
                            "Plugin " + plugin.id() + " contains a null dependency");
                }

                DependencyScope scope = dependency.dependencyScope();

                Plugin dependencyPlugin = plugins.get(dependency.id());

                if (dependencyPlugin == null) {
                    if (scope == DependencyScope.OPTIONAL) {
                        continue;
                    }

                    throw new PluginDependencyException(
                            "Missing dependency: plugin "
                                    + plugin.id()
                                    + " requires "
                                    + dependency.id()
                                    + " >= "
                                    + dependency.version());
                }

                validateVersion(plugin, dependency, dependencyPlugin);

                if (scope == DependencyScope.PROVIDED) {
                    continue;
                }

                graph.get(plugin.id()).add(dependency.id());
            }
        }

        return graph;
    }

    private void validateVersion(
            Plugin plugin,
            Dependency dependency,
            Plugin dependencyPlugin)
            throws PluginDependencyException {

        Version required = dependency.version();

        if (dependencyPlugin.manifest() == null || dependencyPlugin.manifest().version() == null) {
            throw new PluginDependencyException(
                    "Dependency " + dependency.id()
                            + " of plugin " + plugin.id()
                            + " has no version");
        }

        Version actual = dependencyPlugin.manifest().version();

        if (actual.compareTo(required) < 0) {
            throw new PluginDependencyException(
                    "Dependency version mismatch: plugin "
                            + plugin.id()
                            + " requires "
                            + dependency.id()
                            + " >= "
                            + required
                            + " but resolved "
                            + actual);
        }
    }

    private void detectCycles(Map<String, Set<String>> graph) throws PluginDependencyException {
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> path = new ArrayDeque<>();

        for (String pluginId : graph.keySet()) {
            if (visited.contains(pluginId)) {
                continue;
            }

            detectCycle(pluginId, graph, visiting, visited, path);
        }
    }

    private void detectCycle(
            String pluginId,
            Map<String, Set<String>> graph,
            Set<String> visiting,
            Set<String> visited,
            ArrayDeque<String> path)
            throws PluginDependencyException {

        if (visiting.contains(pluginId)) {
            throw cycleException(pluginId, path);
        }

        if (visited.contains(pluginId)) {
            return;
        }

        visiting.add(pluginId);
        path.addLast(pluginId);

        for (String dependencyId : graph.getOrDefault(pluginId, Set.of())) {
            detectCycle(dependencyId, graph, visiting, visited, path);
        }

        path.removeLast();
        visiting.remove(pluginId);
        visited.add(pluginId);
    }

    private PluginDependencyException cycleException(String repeatedId, ArrayDeque<String> path) {
        List<String> cycle = new ArrayList<>();
        boolean collecting = false;

        for (String id : path) {
            if (id.equals(repeatedId)) {
                collecting = true;
            }

            if (collecting) {
                cycle.add(id);
            }
        }

        cycle.add(repeatedId);

        return new PluginDependencyException(
                "Plugin dependency cycle detected: " + String.join(" -> ", cycle));
    }

    private List<Plugin> topologicalSort(
            Map<String, Plugin> plugins,
            Map<String, Set<String>> graph) {

        Map<String, Integer> dependencyCount = new LinkedHashMap<>();
        Map<String, Set<String>> dependents = new LinkedHashMap<>();

        for (String id : graph.keySet()) {
            dependencyCount.put(id, graph.get(id).size());
            dependents.put(id, new LinkedHashSet<>());
        }

        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String pluginId = entry.getKey();

            for (String dependencyId : entry.getValue()) {
                dependents.get(dependencyId).add(pluginId);
            }
        }

        ArrayDeque<String> ready = new ArrayDeque<>();

        for (Map.Entry<String, Integer> entry : dependencyCount.entrySet()) {
            if (entry.getValue() == 0) {
                ready.addLast(entry.getKey());
            }
        }

        List<Plugin> result = new ArrayList<>();

        while (!ready.isEmpty()) {
            String id = ready.removeFirst();
            result.add(plugins.get(id));

            for (String dependent : dependents.get(id)) {
                int remaining = dependencyCount.compute(dependent, (key, value) -> value - 1);

                if (remaining == 0) {
                    ready.addLast(dependent);
                }
            }
        }

        if (result.size() != plugins.size()) {
            throw new IllegalStateException("Dependency graph could not be topologically sorted");
        }

        return List.copyOf(result);
    }
}
