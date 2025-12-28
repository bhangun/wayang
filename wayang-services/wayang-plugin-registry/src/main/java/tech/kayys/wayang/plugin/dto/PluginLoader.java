package tech.kayys.wayang.plugin.dto;

@ApplicationScoped
public class PluginLoader {

    @Inject
    PluginClassLoaderFactory classLoaderFactory;

    @Inject
    PluginDescriptorParser descriptorParser;

    @Inject
    PluginValidator validator;

    public Uni<Plugin> loadPlugin(Path pluginPath) {
        return Uni.createFrom().deferred(() -> {
            // 1. Extract plugin descriptor
            PluginDescriptor descriptor = descriptorParser.parse(pluginPath);

            // 2. Validate plugin
            ValidationResult validation = validator.validate(descriptor, pluginPath);
            if (!validation.isValid()) {
                return Uni.createFrom().failure(
                        new PluginValidationException(validation.getErrors()));
            }

            // 3. Create classloader
            ClassLoader classLoader = classLoaderFactory.createClassLoader(pluginPath, descriptor);

            // 4. Load plugin class
            Class<?> pluginClass = loadPluginClass(descriptor, classLoader);

            // 5. Instantiate plugin
            Plugin plugin = instantiatePlugin(pluginClass, descriptor);

            return Uni.createFrom().item(plugin);
        });
    }

    private Class<?> loadPluginClass(PluginDescriptor descriptor, ClassLoader classLoader)
            throws PluginLoadException {
        try {
            String className = (String) descriptor.metadata()
                    .getOrDefault("mainClass", getDefaultClassName(descriptor));

            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new PluginLoadException(
                    "Failed to load plugin class for: " + descriptor.id(), e);
        }
    }

    private Plugin instantiatePlugin(Class<?> pluginClass, PluginDescriptor descriptor)
            throws PluginLoadException {
        try {
            Object instance = pluginClass.getDeclaredConstructor().newInstance();

            if (!(instance instanceof Plugin)) {
                throw new PluginLoadException(
                        "Plugin class does not implement Plugin interface: " + pluginClass.getName());
            }

            Plugin plugin = (Plugin) instance;
            injectDescriptor(plugin, descriptor);

            return plugin;
        } catch (Exception e) {
            throw new PluginLoadException(
                    "Failed to instantiate plugin: " + descriptor.id(), e);
        }
    }

    private void injectDescriptor(Plugin plugin, PluginDescriptor descriptor) {
        // Use reflection or a DI framework to inject the descriptor
        // For simplicity, we assume plugin has a setDescriptor method
        try {
            Method setDescriptor = plugin.getClass()
                    .getMethod("setDescriptor", PluginDescriptor.class);
            setDescriptor.invoke(plugin, descriptor);
        } catch (Exception e) {
            // Plugin might not have this method, which is OK
            LOG.debugf("Plugin %s does not have setDescriptor method", descriptor.id());
        }
    }

    private String getDefaultClassName(PluginDescriptor descriptor) {
        // Convert plugin ID to class name convention
        // e.g., "my-agent" -> "tech.kayys.wayang.plugin.myagent.MainPlugin"
        String packageName = descriptor.id().replace('-', '.');
        return "tech.kayys.wayang.plugin." + packageName + ".MainPlugin";
    }
}