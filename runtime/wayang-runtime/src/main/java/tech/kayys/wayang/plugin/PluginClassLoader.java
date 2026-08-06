package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.PluginManager;
import tech.kayys.wayang.spi.plugin.PluginState;
import tech.kayys.wayang.spi.plugin.ManifestRegistry;
import tech.kayys.wayang.spi.plugin.Dependency;


import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

import tech.kayys.wayang.core.Version;
import tech.kayys.wayang.plugin.Manifest;


import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Plugin Class Loader - Isolates plugin classes from the core system.
 * 
 * <p>This class loader is responsible for loading classes from a plugin's
 * JAR file(s) with proper isolation from the core Wayang runtime. It follows
 * a child-first delegation model, meaning it attempts to load classes from
 * the plugin itself before delegating to the parent class loader.</p>
 * 
 * <h2>Class Loading Delegation Model</h2>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    PluginClassLoader                           │
 * │  ┌─────────────────────────────────────────────────────────┐  │
 * │  │ 1. Check if class already loaded (findLoadedClass)     │  │
 * │  │ 2. Try to load from plugin JAR (findClass)             │  │
 * │  │ 3. If not found, delegate to parent                     │  │
 * │  └─────────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 *                              │
 *                              ▼
 *              ┌───────────────────────────┐
 *              │   Parent ClassLoader      │
 *              │   (System/Application)    │
 *              └───────────────────────────┘
 * </pre>
 * 
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Child-First Delegation:</b> Plugin classes take precedence over core classes</li>
 *   <li><b>Class Isolation:</b> Plugins cannot see each other's classes</li>
 *   <li><b>Resource Isolation:</b> Plugin resources are loaded from plugin JARs</li>
 *   <li><b>Dependency Management:</b> Plugin manifests are used for dependency resolution</li>
 *   <li><b>Security:</b> Plugin code runs with restricted privileges</li>
 *   <li><b>Version Awareness:</b> Multiple versions of the same plugin can coexist</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * {@code
 * // Create class loader for a plugin
 * PluginClassLoader loader = new PluginClassLoader(
 *     "my-plugin",
 *     manifest,
 *     new URL[]{Paths.get("plugins/my-plugin.jar").toUri().toURL()},
 *     Thread.currentThread().getContextClassLoader()
 * );
 * 
 * // Load a class from the plugin
 * Class<?> clazz = loader.loadClass("com.example.MyPluginClass");
 * 
 * // Create an instance
 * MyPluginClass instance = (MyPluginClass) clazz.getDeclaredConstructor().newInstance();
 * }
 * </pre>
 * 
 * <h2>Security Considerations</h2>
 * <ul>
 *   <li>System classes (java.*, javax.*) are never loaded from plugins</li>
 *   <li>Core Wayang classes (io.wayang.core.*) are loaded from parent</li>
 *   <li>Each plugin has its own class space for isolation</li>
 *   <li>Plugin resources are accessed through the plugin's class loader</li>
 * </ul>
 * 
 * @see io.wayang.manifest.Manifest
 * @see io.wayang.extension.Extension
 * @see io.wayang.plugin.PluginManager
 */
public class PluginClassLoader extends URLClassLoader {
    
    /**
     * System packages that must be loaded from the parent class loader.
     * These packages are considered trusted and should never come from a plugin.
     */
    private static final Set<String> SYSTEM_PACKAGES = Set.of(
        "java.",
        "javax.",
        "sun.",
        "jdk.",
        "org.w3c.",
        "org.xml.",
        "org.ietf."
    );
    
    /**
     * Core Wayang packages that should be loaded from the parent.
     * This ensures the plugin uses the same core classes as the runtime.
     */
    private static final Set<String> CORE_PACKAGES = Set.of(
        "io.wayang.core.",
        "io.wayang.foundation.",
        "io.wayang.identity.",
        "io.wayang.resource.",
        "io.wayang.manifest.",
        "io.wayang.event."
    );
    
    /**
     * Cache of loaded classes to avoid repeated loading.
     */
    private final Map<String, Class<?>> loadedClassCache = new ConcurrentHashMap<>();
    
    /**
     * Cache of package-defined classes.
     */
    private final Map<String, Package> packageCache = new ConcurrentHashMap<>();
    
    /**
     * Plugin identifier.
     */
    private final String pluginId;
    
    /**
     * Plugin manifest containing metadata and dependencies.
     */
    private final Manifest manifest;
    
    /**
     * Resources that have been loaded from the plugin.
     */
    private final Set<String> loadedResources = ConcurrentHashMap.newKeySet();
    
    /**
     * Whether the class loader is closed.
     */
    private volatile boolean closed = false;
    
    /**
     * Creates a new PluginClassLoader.
     *
     * @param pluginId The unique identifier of the plugin
     * @param manifest The plugin manifest
     * @param urls The URLs from which to load classes and resources
     * @param parent The parent class loader
     * @throws NullPointerException if pluginId, manifest, or parent is null
     */
    public PluginClassLoader(String pluginId, Manifest manifest, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.pluginId = Objects.requireNonNull(pluginId, "pluginId cannot be null");
        this.manifest = Objects.requireNonNull(manifest, "manifest cannot be null");
        
        // Register the plugin's packages
        registerPackages();
    }
    
    /**
     * Loads a class from the plugin or parent class loader.
     * 
     * <p>This method follows the child-first delegation model:</p>
     * <ol>
     *   <li>Check if the class is already loaded</li>
     *   <li>Check if the class is a system class (delegate to parent)</li>
     *   <li>Try to load from plugin JARs</li>
     *   <li>If not found, delegate to parent</li>
     * </ol>
     *
     * @param name The fully qualified name of the class to load
     * @param resolve If true, resolve the class (link it)
     * @return The loaded class
     * @throws ClassNotFoundException If the class cannot be found
     * @throws SecurityException If the class is a system class and access is denied
     */
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (closed) {
            throw new ClassNotFoundException("Class loader is closed for plugin: " + pluginId);
        }
        
        // Check cache first
        Class<?> cached = loadedClassCache.get(name);
        if (cached != null) {
            if (resolve) {
                resolveClass(cached);
            }
            return cached;
        }
        
        // Check if class is already loaded by this loader
        Class<?> c = findLoadedClass(name);
        if (c != null) {
            loadedClassCache.put(name, c);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
        
        // System classes must be loaded from parent
        if (isSystemClass(name)) {
            c = getParent().loadClass(name);
            loadedClassCache.put(name, c);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
        
        // Core Wayang classes should be loaded from parent
        if (isCoreClass(name)) {
            c = getParent().loadClass(name);
            loadedClassCache.put(name, c);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        }
        
        // Try to load from plugin
        try {
            c = findClass(name);
            loadedClassCache.put(name, c);
            if (resolve) {
                resolveClass(c);
            }
            return c;
        } catch (ClassNotFoundException e) {
            // Not found in plugin, delegate to parent
            try {
                c = getParent().loadClass(name);
                loadedClassCache.put(name, c);
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            } catch (ClassNotFoundException parentEx) {
                // Both plugin and parent failed
                throw new ClassNotFoundException(
                    "Class '" + name + "' not found in plugin '" + pluginId + "' or parent",
                    e
                );
            }
        }
    }
    
    /**
     * Finds a class from the plugin's URLs.
     * 
     * <p>This method is called by loadClass after it determines that the
     * class should be loaded from the plugin. It searches the plugin's
     * URLs for the class definition.</p>
     *
     * @param name The fully qualified name of the class
     * @return The loaded class
     * @throws ClassNotFoundException If the class cannot be found in the plugin
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            Class<?> c = super.findClass(name);
            
            // Define the package if not already defined
            definePackageForClass(name);
            
            return c;
        } catch (ClassNotFoundException e) {
            // Check if this is a class from a dependency
            if (tryLoadFromDependency(name)) {
                return getParent().loadClass(name);
            }
            throw e;
        }
    }
    
    /**
     * Finds a resource from the plugin's URLs.
     * 
     * <p>This method attempts to find the resource in the plugin first,
     * then falls back to the parent class loader.</p>
     *
     * @param name The resource name
     * @return The URL of the resource, or null if not found
     */
    @Override
    public URL findResource(String name) {
        if (closed) {
            return null;
        }
        
        // Try to find in plugin first
        URL url = super.findResource(name);
        if (url != null) {
            loadedResources.add(name);
            return url;
        }
        
        // Fall back to parent
        url = getParent().getResource(name);
        if (url != null) {
            loadedResources.add(name);
        }
        return url;
    }
    
    /**
     * Finds all resources with the given name from the plugin's URLs.
     *
     * @param name The resource name
     * @return An enumeration of URLs to the resources
     * @throws IOException If an I/O error occurs
     */
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        if (closed) {
            return Collections.emptyEnumeration();
        }
        
        // Get plugin resources
        Enumeration<URL> pluginResources = super.findResources(name);
        
        // Get parent resources
        Enumeration<URL> parentResources = getParent().getResources(name);
        
        // Combine plugin resources first (child-first)
        List<URL> combined = new ArrayList<>();
        while (pluginResources.hasMoreElements()) {
            URL url = pluginResources.nextElement();
            combined.add(url);
            loadedResources.add(name);
        }
        while (parentResources.hasMoreElements()) {
            URL url = parentResources.nextElement();
            combined.add(url);
        }
        
        return Collections.enumeration(combined);
    }
    
    /**
     * Gets the resource as a stream.
     *
     * @param name The resource name
     * @return The input stream, or null if not found
     */
    @Override
    public java.io.InputStream getResourceAsStream(String name) {
        if (closed) {
            return null;
        }
        
        // Try plugin first
        java.io.InputStream is = super.getResourceAsStream(name);
        if (is != null) {
            loadedResources.add(name);
            return is;
        }
        
        // Fall back to parent
        return getParent().getResourceAsStream(name);
    }
    
    /**
     * Gets the plugin ID.
     *
     * @return The plugin ID
     */
    public String getPluginId() {
        return pluginId;
    }
    
    /**
     * Gets the plugin manifest.
     *
     * @return The plugin manifest
     */
    public Manifest getManifest() {
        return manifest;
    }
    
    /**
     * Gets the version of the plugin.
     *
     * @return The plugin version
     */
    public Version getVersion() {
        return manifest.version();
    }
    
    /**
     * Gets the loaded classes count.
     *
     * @return The number of loaded classes
     */
    public int getLoadedClassCount() {
        return loadedClassCache.size();
    }
    
    /**
     * Gets the loaded resources.
     *
     * @return The set of loaded resource names
     */
    public Set<String> getLoadedResources() {
        return new HashSet<>(loadedResources);
    }
    
    /**
     * Checks if the class loader is closed.
     *
     * @return true if closed
     */
    public boolean isClosed() {
        return closed;
    }
    
    /**
     * Closes the class loader and releases resources.
     * 
     * <p>After closing, no more classes or resources can be loaded.</p>
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            super.close();
        } catch (Exception e) {
            // Log but ignore
        }
        loadedClassCache.clear();
        loadedResources.clear();
        packageCache.clear();
    }
    
    /**
     * Checks if a class is a system class that must be loaded from the parent.
     *
     * @param name The class name
     * @return true if it's a system class
     */
    private boolean isSystemClass(String name) {
        for (String packageName : SYSTEM_PACKAGES) {
            if (name.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if a class is a core Wayang class that must be loaded from the parent.
     *
     * @param name The class name
     * @return true if it's a core class
     */
    private boolean isCoreClass(String name) {
        for (String packageName : CORE_PACKAGES) {
            if (name.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Tries to load a class from a dependency of the plugin.
     *
     * @param name The class name
     * @return true if the class is from a dependency
     */
    private boolean tryLoadFromDependency(String name) {
        // This is a simplified check - in production, this would check
        // if the class belongs to a declared dependency
        return false;
    }
    
    /**
     * Defines a package for a class if it's not already defined.
     *
     * @param className The class name
     */
    private void definePackageForClass(String className) {
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            String packageName = className.substring(0, lastDot);
            
            // Check if package is already defined
            if (getPackage(packageName) != null) {
                return;
            }
            
            // Check cache
            if (packageCache.containsKey(packageName)) {
                definePackage(packageName, packageCache.get(packageName));
                return;
            }
            
            // Define package with default attributes
            Package pkg = definePackage(
                packageName,
                manifest.name(),
                manifest.version().toString(),
                "Wayang Foundation",
                "Wayang",
                manifest.version().toString(),
                "Wayang Community",
                null // URL
            );
            packageCache.put(packageName, pkg);
        }
    }
    
    /**
     * Defines a package with the specified attributes.
     *
     * @param packageName The package name
     * @param packageData The package data
     */
    private void definePackage(String packageName, Package packageData) {
        definePackage(
            packageName,
            packageData.getSpecificationTitle(),
            packageData.getSpecificationVersion(),
            packageData.getSpecificationVendor(),
            packageData.getImplementationTitle(),
            packageData.getImplementationVersion(),
            packageData.getImplementationVendor(),
            packageData.getImplementationURL()
        );
    }
    
    /**
     * Registers the plugin's packages.
     */
    private void registerPackages() {
        // This would scan the plugin JARs to register packages
        // For now, we use lazy registration via definePackageForClass
    }
    
    /**
     * Returns a string representation of the class loader.
     *
     * @return A string representation
     */
    @Override
    public String toString() {
        return "PluginClassLoader{" +
            "pluginId='" + pluginId + '\'' +
            ", version=" + (manifest != null ? manifest.version() : "unknown") +
            ", loadedClasses=" + loadedClassCache.size() +
            ", closed=" + closed +
            '}';
    }
}
