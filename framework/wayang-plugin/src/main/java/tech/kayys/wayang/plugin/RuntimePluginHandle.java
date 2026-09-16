package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.Plugin;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Handle to a dynamically loaded runtime plugin, holding references to the plugin,
 * its source JAR file, and its isolated ClassLoader.
 */
public final class RuntimePluginHandle implements AutoCloseable {

    private final Plugin plugin;
    private final Path source;
    private final ClassLoader classLoader;

    public RuntimePluginHandle(
            Plugin plugin,
            Path source,
            ClassLoader classLoader) {

        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.source = Objects.requireNonNull(source, "source cannot be null");
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader cannot be null");
    }

    public Plugin plugin() {
        return plugin;
    }

    public Path source() {
        return source;
    }

    public ClassLoader classLoader() {
        return classLoader;
    }

    @Override
    public void close() throws Exception {
        if (classLoader instanceof AutoCloseable closeable) {
            closeable.close();
        }
    }
}
