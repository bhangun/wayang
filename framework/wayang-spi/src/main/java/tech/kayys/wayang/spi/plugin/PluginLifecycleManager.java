package tech.kayys.wayang.spi.plugin;

/**
 * Interface responsible for managing the lifecycle of plugins (start, stop, enable, disable).
 */
public interface PluginLifecycleManager {
    
    /**
     * Enable a loaded plugin.
     */
    void enablePlugin(String id) throws Exception;
    
    /**
     * Disable a plugin.
     */
    void disablePlugin(String id) throws Exception;
    
    /**
     * Unload a plugin entirely, freeing its resources.
     */
    void unloadPlugin(String id) throws Exception;
    
    /**
     * Gets the state of a specific plugin.
     */
    PluginState getPluginState(String id) throws Exception;
}
