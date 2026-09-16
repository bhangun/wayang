package tech.kayys.wayang.plugin;

/**
 * Indicates a plugin dependency resolution failure (cycles, missing dependencies, version mismatch).
 */
public final class PluginDependencyException extends Exception {

    public PluginDependencyException(String message) {
        super(message);
    }

    public PluginDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
