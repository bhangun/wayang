package tech.kayys.wayang.plugin;

import java.lang.System.Logger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarFile;

import tech.kayys.wayang.core.Version;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.service.ServiceRegistry;

/**
 * Complete Plugin Manager Implementation
 */
public class DefaultPluginManager implements PluginManager {
    
    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, List<Extension>> extensionsByPlugin = new ConcurrentHashMap<>();
    private final Map<Class<? extends Extension>, List<Extension>> extensionsByType = new ConcurrentHashMap<>();
    private final Path pluginsDirectory;
    private final ServiceRegistry serviceRegistry;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Logger logger;
    
    public DefaultPluginManager(Path pluginsDirectory, ServiceRegistry serviceRegistry) {
        this.pluginsDirectory = pluginsDirectory;
        this.serviceRegistry = serviceRegistry;
        this.logger = LoggerFactory.getLogger(DefaultPluginManager.class);
    }
    
    @Override
    public void loadPlugin(Path path) throws Exception {
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Plugin file not found: " + path);
        }
        
        if (!path.toString().endsWith(".jar")) {
            // Try to find the jar in the plugins directory
            String pluginId = path.toString();
            Path jarPath = pluginsDirectory.resolve(pluginId + ".jar");
            if (Files.exists(jarPath)) {
                path = jarPath;
            } else {
                throw new IllegalArgumentException("Plugin must be a JAR file: " + path);
            }
        }
        
        // Read manifest from JAR
        Manifest manifest = readManifestFromJar(path);
        if (manifest == null) {
            throw new IllegalArgumentException("No manifest found in JAR: " + path);
        }
        
        // Check if already loaded
        if (plugins.containsKey(manifest.id())) {
            throw new IllegalStateException("Plugin already loaded: " + manifest.id());
        }
        
        // Create class loader
        URL[] urls = {path.toUri().toURL()};
        PluginClassLoader classLoader = new PluginClassLoader(
            manifest.id(),
            manifest,
            urls,
            Thread.currentThread().getContextClassLoader()
        );
        
        // Load extensions
        List<Extension> extensions = loadExtensions(manifest, classLoader);
        
        // Create plugin
        Plugin plugin = new DefaultPlugin(
            manifest,
            classLoader,
            extensions,
            PluginState.LOADED
        );
        
        // Register
        plugins.put(manifest.id(), plugin);
        extensionsByPlugin.put(manifest.id(), extensions);
        
        for (Extension extension : extensions) {
            extensionsByType.computeIfAbsent(extension.getClass().asSubclass(Extension.class), 
                k -> new CopyOnWriteArrayList<>()).add(extension);
        }
        
        logger.info("Loaded plugin: {} ({})", manifest.name(), manifest.id());
    }
    
    @Override
    public void loadPlugin(Manifest manifest, ClassLoader classLoader) throws Exception {
        if (plugins.containsKey(manifest.id())) {
            throw new IllegalStateException("Plugin already loaded: " + manifest.id());
        }
        
        // Load extensions from class loader
        List<Extension> extensions = loadExtensions(manifest, classLoader);
        
        Plugin plugin = new DefaultPlugin(
            manifest,
            classLoader,
            extensions,
            PluginState.LOADED
        );
        
        plugins.put(manifest.id(), plugin);
        extensionsByPlugin.put(manifest.id(), extensions);
        
        for (Extension extension : extensions) {
            extensionsByType.computeIfAbsent(extension.getClass().asSubclass(Extension.class),
                k -> new CopyOnWriteArrayList<>()).add(extension);
        }
        
        logger.info("Loaded plugin: {} ({})", manifest.name(), manifest.id());
    }
    
    @Override
    public void unloadPlugin(String id) throws Exception {
        Plugin plugin = plugins.get(id);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + id);
        }
        
        // Stop plugin
        if (plugin.state() == PluginState.ACTIVE) {
            disablePlugin(id);
        }
        
        // Remove extensions
        extensionsByPlugin.remove(id);
        for (List<Extension> extensions : extensionsByType.values()) {
            extensions.removeIf(e -> {
                Plugin p = getPluginForExtension(e);
                return p != null && p.id().equals(id);
            });
        }
        
        plugins.remove(id);
        logger.info("Unloaded plugin: {}", id);
    }
    
    @Override
    public Optional<Plugin> getPlugin(String id) {
        return Optional.ofNullable(plugins.get(id));
    }
    
    @Override
    public List<Plugin> getPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    @Override
    public List<Extension> getExtensions(String pluginId) {
        return extensionsByPlugin.getOrDefault(pluginId, List.of());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Extension> List<T> getExtensions(Class<T> type) {
        List<Extension> extensions = extensionsByType.getOrDefault(type, List.of());
        return extensions.stream()
            .filter(type::isInstance)
            .map(e -> (T) e)
            .toList();
    }
    
    @Override
    public void enablePlugin(String id) throws Exception {
        Plugin plugin = plugins.get(id);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + id);
        }
        
        if (plugin.state() == PluginState.ACTIVE) {
            return;
        }
        
        // Resolve dependencies
        resolveDependencies(plugin);
        
        // Initialize extensions
        for (Extension extension : plugin.extensions()) {
            extension.initialize();
        }
        
        // Start plugin
        plugin.start();
        
        logger.info("Enabled plugin: {} ({})", plugin.manifest().name(), id);
    }
    
    @Override
    public void disablePlugin(String id) throws Exception {
        Plugin plugin = plugins.get(id);
        if (plugin == null) {
            throw new IllegalArgumentException("Plugin not found: " + id);
        }
        
        if (plugin.state() == PluginState.STOPPED) {
            return;
        }
        
        // Stop plugin
        plugin.stop();
        
        // Shutdown extensions
        for (Extension extension : plugin.extensions()) {
            try {
                extension.shutdown();
            } catch (Exception e) {
                logger.warn("Error shutting down extension: {}", e.getMessage());
            }
        }
        
        logger.info("Disabled plugin: {} ({})", plugin.manifest().name(), id);
    }
    
    private Manifest readManifestFromJar(Path path) throws Exception {
        try (JarFile jar = new JarFile(path.toFile())) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                return null;
            }
            
            java.util.jar.Attributes attributes = manifest.getMainAttributes();
            String pluginId = attributes.getValue("Wayang-Plugin-Id");
            if (pluginId == null) {
                return null;
            }
            
            return new Manifest(
                pluginId,
                Version.parse(attributes.getValue("Wayang-Plugin-Version")),
                attributes.getValue("Wayang-Plugin-Name"),
                attributes.getValue("Wayang-Plugin-Description"),
                attributes.getValue("Wayang-Plugin-Main-Class"),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Map.of(),
                List.of(),
                attributes.getValue("Wayang-Plugin-License"),
                null,
                null,
                Map.of()
            );
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Extension> loadExtensions(Manifest manifest, ClassLoader classLoader) throws Exception {
        List<Extension> extensions = new ArrayList<>();
        
        if (manifest.mainClass() != null) {
            Class<?> mainClass = classLoader.loadClass(manifest.mainClass());
            if (Extension.class.isAssignableFrom(mainClass)) {
                Extension extension = (Extension) mainClass.getDeclaredConstructor().newInstance();
                extensions.add(extension);
            }
        }
        
        // Discover extensions using service loader
        ServiceLoader<Extension> loader = ServiceLoader.load(Extension.class, classLoader);
        for (Extension extension : loader) {
            extensions.add(extension);
        }
        
        return extensions;
    }
    
    private void resolveDependencies(Plugin plugin) throws Exception {
        // Check dependencies
        for (tech.kayys.wayang.manifest.Dependency dependency : plugin.manifest().dependencies()) {
            if (!plugins.containsKey(dependency.id())) {
                throw new IllegalStateException("Missing dependency: " + dependency.id());
            }
        }
    }
    
    private Plugin getPluginForExtension(Extension extension) {
        for (Plugin plugin : plugins.values()) {
            if (plugin.extensions().contains(extension)) {
                return plugin;
            }
        }
        return null;
    }
}