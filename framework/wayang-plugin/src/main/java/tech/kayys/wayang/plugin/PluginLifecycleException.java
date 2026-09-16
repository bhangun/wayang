package tech.kayys.wayang.plugin;

/**
 * Indicates a plugin lifecycle operation failure.
 */
public final class PluginLifecycleException extends Exception {

    public PluginLifecycleException(String message) {
        super(message);
    }

    public PluginLifecycleException(
            String message,
            Throwable cause) {
        super(message, cause);
    }
}
