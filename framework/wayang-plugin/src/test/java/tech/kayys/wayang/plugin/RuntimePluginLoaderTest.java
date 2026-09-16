package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.ManifestId;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginManifestException;
import tech.kayys.wayang.spi.plugin.PluginState;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimePluginLoaderTest {

    private final RuntimePluginLoader loader = new RuntimePluginLoader();

    @Test
    void rejectsNonexistentJar() {
        Path missing = Path.of("target/does-not-exist.jar");

        PluginManifestException ex = assertThrows(
                PluginManifestException.class,
                () -> loader.load(missing)
        );

        assertTrue(ex.getMessage().contains("does not exist"));
    }

    @Test
    void rejectsNonJarFile(@TempDir Path tempDir) throws IOException {
        Path textFile = tempDir.resolve("plugin.txt");
        Files.writeString(textFile, "not a jar");

        PluginManifestException ex = assertThrows(
                PluginManifestException.class,
                () -> loader.load(textFile)
        );

        assertTrue(ex.getMessage().contains(".jar extension"));
    }

    @Test
    void rejectsJarWithoutPluginProvider(@TempDir Path tempDir) throws IOException {
        Path jarPath = tempDir.resolve("empty.jar");
        createJar(jarPath, null);

        PluginManifestException ex = assertThrows(
                PluginManifestException.class,
                () -> loader.load(jarPath)
        );

        assertTrue(ex.getMessage().contains("No Wayang Plugin implementation found"));
    }

    @Test
    void loadsValidPluginFromJar(@TempDir Path tempDir) throws Exception {
        Path jarPath = tempDir.resolve("valid-plugin.jar");
        createJar(jarPath, ValidTestPlugin.class.getName());

        try (RuntimePluginHandle handle = loader.load(jarPath)) {
            assertNotNull(handle);
            assertEquals("valid.test.plugin", handle.plugin().id());
            assertEquals(jarPath.toAbsolutePath().normalize(), handle.source());
            assertNotNull(handle.classLoader());
        }
    }

    @Test
    void rejectsMultiplePluginProviders(@TempDir Path tempDir) throws IOException {
        Path jarPath = tempDir.resolve("multi-plugin.jar");
        String content = ValidTestPlugin.class.getName() + "\n" + AnotherValidPlugin.class.getName();
        createJar(jarPath, content);

        PluginManifestException ex = assertThrows(
                PluginManifestException.class,
                () -> loader.load(jarPath)
        );

        assertTrue(ex.getMessage().contains("Multiple Wayang Plugin implementations found"));
    }

    @Test
    void rejectsPluginWithInvalidManifest(@TempDir Path tempDir) throws IOException {
        Path jarPath = tempDir.resolve("invalid-manifest.jar");
        createJar(jarPath, InvalidManifestPlugin.class.getName());

        assertThrows(
                PluginManifestException.class,
                () -> loader.load(jarPath)
        );
    }

    @Test
    void runtimePluginManagerLifecycleFlow(@TempDir Path tempDir) throws Exception {
        Path jarPath = tempDir.resolve("valid-plugin.jar");
        createJar(jarPath, ValidTestPlugin.class.getName());

        DefaultPluginRegistry registry = new DefaultPluginRegistry();
        DefaultPluginLifecycleManager lifecycleManager = new DefaultPluginLifecycleManager(registry);
        RuntimePluginManager manager = new RuntimePluginManager(loader, registry, lifecycleManager);

        Plugin installedPlugin = manager.install(jarPath);
        assertEquals("valid.test.plugin", installedPlugin.id());
        assertTrue(manager.handle("valid.test.plugin").isPresent());

        // Enable
        manager.enable("valid.test.plugin");
        assertEquals(PluginState.ACTIVE, lifecycleManager.getPluginState("valid.test.plugin"));

        // Disable
        manager.disable("valid.test.plugin");
        assertEquals(PluginState.STOPPED, lifecycleManager.getPluginState("valid.test.plugin"));

        // Uninstall
        manager.uninstall("valid.test.plugin");
        assertTrue(manager.handle("valid.test.plugin").isEmpty());
    }

    private static void createJar(Path jarPath, String serviceProviderContent) throws IOException {
        try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            if (serviceProviderContent != null) {
                JarEntry entry = new JarEntry("META-INF/services/tech.kayys.wayang.spi.plugin.Plugin");
                jos.putNextEntry(entry);
                jos.write(serviceProviderContent.getBytes(StandardCharsets.UTF_8));
                jos.closeEntry();
            }
        }
    }

    public static final class ValidTestPlugin implements Plugin {

        @Override
        public String id() {
            return "valid.test.plugin";
        }

        @Override
        public Manifest manifest() {
            return DefaultManifest.builder()
                    .id("valid.test.plugin")
                    .name("valid-test-plugin")
                    .version(Version.VERSION_1_0_0)
                    .apiVersion(Version.VERSION_1_0_0)
                    .mainClass(ValidTestPlugin.class.getName())
                    .build();
        }

        @Override
        public PluginState state() {
            return PluginState.LOADED;
        }

        @Override
        public ClassLoader classLoader() {
            return getClass().getClassLoader();
        }

        @Override
        public List<Extension> extensions() {
            return List.of();
        }

        @Override
        public void initialize() {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }

    public static final class AnotherValidPlugin implements Plugin {

        @Override
        public String id() {
            return "another.valid.plugin";
        }

        @Override
        public Manifest manifest() {
            return DefaultManifest.builder()
                    .id("another.valid.plugin")
                    .name("another-valid-plugin")
                    .version(Version.VERSION_1_0_0)
                    .apiVersion(Version.VERSION_1_0_0)
                    .mainClass(AnotherValidPlugin.class.getName())
                    .build();
        }

        @Override
        public PluginState state() {
            return PluginState.LOADED;
        }

        @Override
        public ClassLoader classLoader() {
            return getClass().getClassLoader();
        }

        @Override
        public List<Extension> extensions() {
            return List.of();
        }

        @Override
        public void initialize() {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }

    public static final class InvalidManifestPlugin implements Plugin {

        @Override
        public String id() {
            return "invalid.manifest.plugin";
        }

        @Override
        public Manifest manifest() {
            // Null manifest will fail validation
            return null;
        }

        @Override
        public PluginState state() {
            return PluginState.LOADED;
        }

        @Override
        public ClassLoader classLoader() {
            return getClass().getClassLoader();
        }

        @Override
        public List<Extension> extensions() {
            return List.of();
        }

        @Override
        public void initialize() {
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }
    }
}
