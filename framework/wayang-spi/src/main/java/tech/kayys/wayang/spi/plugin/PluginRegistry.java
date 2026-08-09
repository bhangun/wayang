package tech.kayys.wayang.spi.plugin;

import java.util.List;
import java.util.Optional;
import tech.kayys.wayang.extension.Extension;

/**
 * Interface responsible for querying and retrieving loaded plugins and extensions.
 */
public interface PluginRegistry {
    
    /**
     * Get a plugin by its ID.
     */
    Optional<Plugin> getPlugin(String id) throws Exception;
    
    /**
     * Get all loaded plugins.
     */
    List<Plugin> getPlugins() throws Exception;
    
    /**
     * Get extensions associated with a specific plugin.
     */
    List<Extension> getExtensions(String pluginId) throws Exception;
    
    /**
     * Get all extensions of a specific type across all plugins.
     */
    <T extends Extension> List<T> getExtensions(Class<T> type) throws Exception;
}
