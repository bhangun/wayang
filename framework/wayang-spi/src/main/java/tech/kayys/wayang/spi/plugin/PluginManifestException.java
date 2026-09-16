package tech.kayys.wayang.spi.plugin;

/**
 * Raised when a plugin manifest violates the Wayang plugin contract.
 */
public class PluginManifestException extends RuntimeException {

    public PluginManifestException(String message) {
        super(message);
    }

    public PluginManifestException(String message, Throwable cause) {
        super(message, cause);
    }
}
