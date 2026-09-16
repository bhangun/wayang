package tech.kayys.wayang.spi.plugin;

import java.util.List;

/**
 * Extension point for plugin dependency resolution and ordering.
 */
public interface PluginDependencyResolver {

    /**
     * Resolves the complete plugin dependency graph.
     *
     * @param plugins plugins participating in resolution
     * @return plugins ordered so dependencies appear before dependents
     * @throws Exception when dependencies cannot be resolved
     */
    List<Plugin> resolve(List<Plugin> plugins) throws Exception;
}
