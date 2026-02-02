package tech.kayys.wayang.plugin;

/**
 * Base interface for all Wayang plugins.
 */
public interface WayangPlugin {
    /**
     * Unique identifier for the plugin.
     */
    String id();

    /**
     * Human-readable name.
     */
    String name();

    /**
     * Plugin version.
     */
    String version();

    /**
     * Plugin description.
     */
    String description();
}
