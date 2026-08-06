package tech.kayys.wayang.plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;

/**
 * Default Plugin Implementation
 */
public class DefaultPlugin implements Plugin {
    
    private final Manifest manifest;
    private final ClassLoader classLoader;
    private final List<Extension> extensions;
    private volatile PluginState state;
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    
    public DefaultPlugin(Manifest manifest, ClassLoader classLoader, List<Extension> extensions, PluginState state) {
        this.manifest = manifest;
        this.classLoader = classLoader;
        this.extensions = List.copyOf(extensions);
        this.state = state;
    }
    
    @Override
    public String id() {
        return manifest.id();
    }
    
    @Override
    public Manifest manifest() {
        return manifest;
    }
    
    @Override
    public PluginState state() {
        return state;
    }
    
    @Override
    public ClassLoader classLoader() {
        return classLoader;
    }
    
    @Override
    public List<Extension> extensions() {
        return extensions;
    }
    
    @Override
    public void initialize() throws Exception {
        for (Extension extension : extensions) {
            extension.initialize();
        }
        state = PluginState.RESOLVED;
    }
    
    @Override
    public void start() throws Exception {
        state = PluginState.STARTING;
        // Start extensions
        state = PluginState.ACTIVE;
    }
    
    @Override
    public void stop() throws Exception {
        state = PluginState.STOPPING;
        // Stop extensions
        state = PluginState.STOPPED;
    }
    
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
