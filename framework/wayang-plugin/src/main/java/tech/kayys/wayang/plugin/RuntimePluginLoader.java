package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginManifestException;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * Loads dynamic external plugins from standalone JAR files with classloader isolation.
 */
public final class RuntimePluginLoader {

    private final ClassLoader parentClassLoader;
    private final DefaultManifestValidator manifestValidator;

    public RuntimePluginLoader() {
        this(
                RuntimePluginLoader.class.getClassLoader(),
                new DefaultManifestValidator()
        );
    }

    public RuntimePluginLoader(
            ClassLoader parentClassLoader,
            DefaultManifestValidator manifestValidator) {

        this.parentClassLoader = Objects.requireNonNull(
                parentClassLoader,
                "parentClassLoader cannot be null");

        this.manifestValidator = Objects.requireNonNull(
                manifestValidator,
                "manifestValidator cannot be null");
    }

    public RuntimePluginHandle load(Path jar)
            throws IOException, PluginManifestException {

        Objects.requireNonNull(jar, "jar cannot be null");

        Path normalized = jar.toAbsolutePath().normalize();

        validateJar(normalized);

        URLClassLoader classLoader = new URLClassLoader(
                new URL[]{normalized.toUri().toURL()},
                parentClassLoader
        );

        try {
            List<Plugin> plugins = discoverPlugins(classLoader);

            if (plugins.isEmpty()) {
                throw new PluginManifestException(
                        "No Wayang Plugin implementation found in: " + normalized);
            }

            if (plugins.size() > 1) {
                throw new PluginManifestException(
                        "Multiple Wayang Plugin implementations found in: "
                                + normalized
                                + ". A runtime plugin JAR must contain exactly one Plugin implementation.");
            }

            Plugin plugin = plugins.getFirst();

            validatePlugin(plugin);

            return new RuntimePluginHandle(
                    plugin,
                    normalized,
                    classLoader
            );

        } catch (Exception e) {
            try {
                classLoader.close();
            } catch (IOException closeFailure) {
                e.addSuppressed(closeFailure);
            }

            if (e instanceof PluginManifestException manifestException) {
                throw manifestException;
            }

            if (e instanceof IOException ioException) {
                throw ioException;
            }

            throw new PluginManifestException(
                    "Unable to load plugin from " + normalized,
                    e
            );
        }
    }

    private void validateJar(Path jar) {
        if (!Files.exists(jar)) {
            throw new PluginManifestException(
                    "Plugin JAR does not exist: " + jar);
        }

        if (!Files.isRegularFile(jar)) {
            throw new PluginManifestException(
                    "Plugin path is not a regular file: " + jar);
        }

        if (!Files.isReadable(jar)) {
            throw new PluginManifestException(
                    "Plugin JAR is not readable: " + jar);
        }

        String filename = jar.getFileName().toString();

        if (!filename.endsWith(".jar")) {
            throw new PluginManifestException(
                    "Plugin file must have .jar extension: " + jar);
        }
    }

    private List<Plugin> discoverPlugins(ClassLoader classLoader) {
        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(
                Plugin.class,
                classLoader
        );

        List<Plugin> plugins = new ArrayList<>();

        for (Plugin plugin : serviceLoader) {
            plugins.add(plugin);
        }

        return List.copyOf(plugins);
    }

    private void validatePlugin(Plugin plugin) {
        if (plugin == null) {
            throw new PluginManifestException("Plugin provider returned null");
        }

        if (plugin.id() == null || plugin.id().isBlank()) {
            throw new PluginManifestException("Plugin ID cannot be null or blank");
        }

        if (plugin.manifest() == null) {
            throw new PluginManifestException("Plugin " + plugin.id() + " returned a null manifest");
        }

        manifestValidator.validate(plugin.manifest());
    }
}
