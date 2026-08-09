package tech.kayys.wayang.spi.plugin;

import java.nio.file.Path;

/**
 * Interface responsible for loading plugins from various sources (SPI, ClassLoader, etc.).
 */
public interface PluginLoader {
    /**
     * Load a plugin from a filesystem path.
     */
    void loadPlugin(Path path) throws Exception;
    
    /**
     * Load a plugin from a manifest and classloader.
     */
    void loadPlugin(Manifest manifest, ClassLoader classLoader) throws Exception;
}
