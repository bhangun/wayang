package tech.kayys.wayang.plugin.loader;

import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginLoader;
import tech.kayys.wayang.spi.plugin.Manifest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Discovers and loads plugins from the classpath using Java's native ServiceLoader SPI.
 */
public class ServiceLoaderPluginLoader implements PluginLoader {
    private static final Logger LOGGER = Logger.getLogger(ServiceLoaderPluginLoader.class.getName());

    private final List<Plugin> discoveredPlugins = new ArrayList<>();

    @Override
    public void loadPlugin(Path path) throws Exception {
        throw new UnsupportedOperationException("ServiceLoaderPluginLoader only discovers from the existing classpath, not from specific paths.");
    }

    @Override
    public void loadPlugin(Manifest manifest, ClassLoader classLoader) throws Exception {
        throw new UnsupportedOperationException("ServiceLoaderPluginLoader only discovers from the existing classpath.");
    }

    /**
     * Discovers all plugins registered via META-INF/services/tech.kayys.wayang.spi.plugin.Plugin
     */
    public List<Plugin> discoverPlugins() {
        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class);
        for (Plugin plugin : serviceLoader) {
            LOGGER.info("Discovered SPI Plugin: " + plugin.id());
            discoveredPlugins.add(plugin);
        }
        return discoveredPlugins;
    }
}
