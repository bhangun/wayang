package tech.kayys.wayang.plugin.loader;

import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginLoader;
import tech.kayys.wayang.spi.plugin.Manifest;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * Loads dynamic external plugins from .jar files.
 */
public class ClassLoaderPluginLoader implements PluginLoader {
    private static final Logger LOGGER = Logger.getLogger(ClassLoaderPluginLoader.class.getName());

    private final List<Plugin> loadedPlugins = new ArrayList<>();

    @Override
    public void loadPlugin(Path path) throws Exception {
        LOGGER.info("Loading plugin from jar: " + path.toAbsolutePath());
        
        URL[] urls = { path.toUri().toURL() };
        URLClassLoader childLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
        
        // Use ServiceLoader on the custom classloader to find the entry point
        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, childLoader);
        
        boolean found = false;
        for (Plugin plugin : serviceLoader) {
            LOGGER.info("Successfully loaded external plugin: " + plugin.id());
            loadedPlugins.add(plugin);
            found = true;
        }
        
        if (!found) {
            LOGGER.warning("No Plugin implementations found in " + path);
        }
    }

    @Override
    public void loadPlugin(Manifest manifest, ClassLoader classLoader) throws Exception {
        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, classLoader);
        for (Plugin plugin : serviceLoader) {
            if (plugin.id().equals(manifest.id())) {
                loadedPlugins.add(plugin);
            }
        }
    }
    
    public List<Plugin> getLoadedPlugins() {
        return loadedPlugins;
    }
}
