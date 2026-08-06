package tech.kayys.wayang.spi.plugin;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;


/**
 * Plugin Manager - manages plugins.
 */
public interface PluginManager extends Extension {
    
    /**
     * Load a plugin
     */
    void loadPlugin(Path path) throws Exception;
    
    /**
     * Load a plugin from manifest
     */
    void loadPlugin(Manifest manifest, ClassLoader classLoader) throws Exception;
    
    /**
     * Unload a plugin
     */
    void unloadPlugin(String id) throws Exception;
    
    /**
     * Get a plugin
     */
    Optional<Plugin> getPlugin(String id) throws Exception;
    
    /**
     * Get all plugins
     */
    List<Plugin> getPlugins() throws Exception;
    
    /**
     * Get extensions for a plugin
     */
    List<Extension> getExtensions(String pluginId) throws Exception;
    
    /**
     * Get extensions by type
     */
    <T extends Extension> List<T> getExtensions(Class<T> type) throws Exception;
    
    /**
     * Enable a plugin
     */
    void enablePlugin(String id) throws Exception;
    
    /**
     * Disable a plugin
     */
    void disablePlugin(String id) throws Exception;
    
    /**
     * Get plugin state
     */
    default PluginState getPluginState(String id) throws Exception {
        return getPlugin(id).map(Plugin::state).orElse(PluginState.ERROR);
    }
    
    /**
     * Scan for plugins
     */
    default List<Path> scanForPlugins(Path directory) throws Exception {
        List<Path> plugins = new ArrayList<>();
        if (Files.exists(directory)) {
            try (Stream<Path> stream = Files.walk(directory)) {
                plugins = stream
                    .filter(p -> p.toString().endsWith(".jar"))
                    .collect(Collectors.toList());
            }
        }
        return plugins;
    }
}