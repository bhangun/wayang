package tech.kayys.wayang.spi.plugin;

/**
 * Defines how a plugin dependency participates in plugin resolution.
 */
public enum DependencyScope {

    /**
     * Dependency must exist and be resolved for the plugin to be resolved.
     */
    REQUIRED,

    /**
     * Dependency may be absent.
     */
    OPTIONAL,

    /**
     * Dependency is supplied by the host/runtime.
     */
    PROVIDED
}
