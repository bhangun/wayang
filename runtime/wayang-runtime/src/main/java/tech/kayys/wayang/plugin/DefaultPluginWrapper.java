package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.PluginManager;
import tech.kayys.wayang.spi.plugin.PluginState;
import tech.kayys.wayang.spi.plugin.ManifestRegistry;
import tech.kayys.wayang.spi.plugin.Dependency;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.core.Version;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;

/**
 * Simple Plugin Wrapper
 */
class DefaultPluginWrapper implements Plugin {
    private final String id;
    private final Manifest manifest;
    private final ClassLoader classLoader;
    private final List<Extension> extensions;
    private volatile PluginState state = PluginState.LOADED;
    
    public DefaultPluginWrapper(String id, Path path) {
        this.id = id;
        this.manifest = new Manifest(
            id, Version.VERSION_1_0_0, id, null, null,
            List.of(), List.of(), List.of(), List.of(),
            Map.of(), List.of(), null, null, null, Map.of()
        );
        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.extensions = new ArrayList<>();
    }
    
    public DefaultPluginWrapper(Manifest manifest, ClassLoader classLoader, List<Extension> extensions) {
        this.id = manifest.id();
        this.manifest = manifest;
        this.classLoader = classLoader;
        this.extensions = extensions;
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public Manifest manifest() { return manifest; }
    
    @Override
    public PluginState state() { return state; }
    
    @Override
    public ClassLoader classLoader() { return classLoader; }
    
    @Override
    public List<Extension> extensions() { return extensions; }
    
    @Override
    public void initialize() throws Exception {
        state = PluginState.RESOLVED;
        for (Extension extension : extensions) {
            extension.initialize();
        }
    }
    
    @Override
    public void start() throws Exception {
        state = PluginState.STARTING;
        state = PluginState.ACTIVE;
    }
    
    @Override
    public void stop() throws Exception {
        state = PluginState.STOPPING;
        for (Extension extension : extensions) {
            try {
                extension.shutdown();
            } catch (Exception e) {
                // Ignore
            }
        }
        state = PluginState.STOPPED;
    }
}
