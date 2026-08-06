package tech.kayys.wayang.kernel.bootstrap;

import java.util.List;

/**
 * Bootstraps the Wayang platform.
 */
public class Bootstrap {

    private static WayangKernel kernel;

    /**
     * Boots the platform with default configuration.
     */
    public static synchronized WayangKernel boot() {
        if (kernel != null && kernel.isRunning()) {
            return kernel;
        }

        // Create kernel
        kernel = new DefaultWayangKernel();

        // Discover plugins
        kernel.plugins().scan(Path.of("plugins"));

        // Register built-in runtimes
        registerDefaultRuntimes(kernel);

        // Register capabilities
        registerDefaultCapabilities(kernel);

        // Start the kernel
        kernel.start();

        return kernel;
    }

    /**
     * Boots the platform with custom configuration.
     */
    public static synchronized WayangKernel boot(BootstrapConfiguration config) {
        if (kernel != null && kernel.isRunning()) {
            return kernel;
        }

        // Create kernel with config
        kernel = new DefaultWayangKernel();

        // Apply configuration
        for (BootstrapConfigurator configurator : config.getConfigurators()) {
            configurator.configure(kernel);
        }

        // Discover plugins
        if (config.getPluginPaths() != null) {
            for (Path path : config.getPluginPaths()) {
                kernel.plugins().scan(path);
            }
        }

        // Register runtimes
        if (config.getRuntimes() != null) {
            for (Runtime runtime : config.getRuntimes()) {
                kernel.runtimes().register(runtime);
            }
        }

        // Register capabilities
        if (config.getCapabilities() != null) {
            for (Capability capability : config.getCapabilities()) {
                kernel.capabilities().register(capability);
            }
        }

        // Start the kernel
        kernel.start();

        return kernel;
    }

    /**
     * Shuts down the platform.
     */
    public static synchronized void shutdown() {
        if (kernel != null && kernel.isRunning()) {
            kernel.stop();
        }
        kernel = null;
    }

    /**
     * Returns the running kernel.
     */
    public static synchronized WayangKernel getKernel() {
        if (kernel == null) {
            return boot();
        }
        return kernel;
    }

    private static void registerDefaultRuntimes(WayangKernel kernel) {
        // Register runtimes in dependency order
        // This would be done by the runtime modules
        // Example:
        // kernel.runtimes().register(new DefaultToolRuntime());
        // kernel.runtimes().register(new DefaultMemoryRuntime());
        // etc.
    }

    private static void registerDefaultCapabilities(WayangKernel kernel) {
        // Register default capabilities
        // This would be done by the capability modules
    }
}