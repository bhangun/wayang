package tech.kayys.wayang.plugin;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.plugin.executor.ExecutorPlugin;
import tech.kayys.wayang.plugin.executor.ExecutorRegistration;
import tech.kayys.wayang.plugin.executor.ExecutorStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the lifecycle and registration of Wayang plugins in the Control
 * Plane.
 */
@ApplicationScoped
public class ControlPlanePluginManager {

    private static final Logger LOG = LoggerFactory.getLogger(ControlPlanePluginManager.class);

    private final Map<String, PluginRegistration> registeredPlugins = new ConcurrentHashMap<>();

    @Inject
    ControlPlaneExecutorRegistry executorRegistry;

    /**
     * Register a new Wayang plugin.
     * Mechanisms like CDI or explicit calls can use this.
     */
    public void register(WayangPlugin plugin) {
        LOG.info("Registering plugin: {} ({})", plugin.name(), plugin.version());

        PluginRegistration registration = new PluginRegistration();
        registration.pluginId = plugin.id();
        registration.pluginName = plugin.name();
        registration.version = plugin.version();
        registration.registeredAt = Instant.now();

        // Handle specific plugin types
        if (plugin instanceof ExecutorPlugin executorPlugin) {
            handleExecutorPlugin(executorPlugin, registration);
        }

        registeredPlugins.put(plugin.id(), registration);
        LOG.info("Plugin registered successfully: {}", plugin.id());
    }

    private void handleExecutorPlugin(ExecutorPlugin plugin, PluginRegistration registration) {
        LOG.info("Processing executor plugin capabilities: {}", plugin.capabilities());

        // Register as an in-process executor for now, or assume it provides one
        // ideally ExecutorPlugin would have a method to return the executor instance or
        // factory
        // For SPI simplicity, we might assume the plugin IS the factory or provider.
        // Let's assume for now we register a generic/proxy registration or the plugin
        // itself if it fits.
        // In a real scenario, ExecutorPlugin might expose `createExecutor()` or
        // similar.

        // Here we just register metadata and capabilities
        ExecutorRegistration execReg = new ExecutorRegistration();
        execReg.executorId = plugin.id(); // Use plugin ID as executor ID for simplicity
        execReg.capabilities = plugin.capabilities();
        execReg.status = ExecutorStatus.HEALTHY;
        execReg.inProcess = true;

        // We register it into the ExecutorRegistry
        executorRegistry.register(execReg).subscribe().with(
                v -> LOG.info("Executor capability registered for plugin: {}", plugin.id()),
                f -> LOG.error("Failed to register executor for plugin: {}", plugin.id(), f));

        registration.registeredExecutors.add(plugin.id());
    }

    public List<PluginRegistration> getRegisteredPlugins() {
        return new ArrayList<>(registeredPlugins.values());
    }

    public PluginRegistration getPlugin(String pluginId) {
        return registeredPlugins.get(pluginId);
    }
}
