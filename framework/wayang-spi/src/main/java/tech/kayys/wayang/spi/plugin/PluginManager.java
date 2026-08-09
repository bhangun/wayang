package tech.kayys.wayang.spi.plugin;

import tech.kayys.wayang.extension.Extension;

/**
 * PluginManager aggregates the separated SPI interfaces for Plugins.
 * It provides a unified entry point for loaders, registries, and lifecycle management,
 * adhering to the Interface Segregation Principle by composing smaller interfaces.
 */
public interface PluginManager extends 
    PluginLoader, 
    PluginRegistry, 
    PluginLifecycleManager, 
    Extension {
    
}