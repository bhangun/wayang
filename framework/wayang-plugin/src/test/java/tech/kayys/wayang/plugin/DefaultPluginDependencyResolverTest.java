package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.spi.plugin.Dependency;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginState;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultPluginDependencyResolverTest {

    private final DefaultPluginDependencyResolver resolver =
            new DefaultPluginDependencyResolver();

    @Test
    void resolvesDependenciesBeforeDependents() throws Exception {
        TestPlugin d = plugin("D", "1.0.0");
        TestPlugin b = plugin("B", "1.0.0", Dependency.required("D", "1.0.0"));
        TestPlugin c = plugin("C", "1.0.0");
        TestPlugin a = plugin(
                "A",
                "1.0.0",
                Dependency.required("B", "1.0.0"),
                Dependency.required("C", "1.0.0")
        );

        List<Plugin> result = resolver.resolve(List.of(a, b, c, d));

        List<String> order = result.stream().map(Plugin::id).toList();
        assertTrue(order.indexOf("D") < order.indexOf("B"));
        assertTrue(order.indexOf("B") < order.indexOf("A"));
        assertTrue(order.indexOf("C") < order.indexOf("A"));
    }

    @Test
    void missingRequiredDependencyFails() {
        TestPlugin a = plugin("A", "1.0.0", Dependency.required("B", "1.0.0"));

        assertThrows(
                PluginDependencyException.class,
                () -> resolver.resolve(List.of(a))
        );
    }

    @Test
    void optionalMissingDependencyIsAllowed() throws Exception {
        TestPlugin a = plugin("A", "1.0.0", Dependency.optional("B", "1.0.0"));

        List<Plugin> result = resolver.resolve(List.of(a));

        assertEquals(
                List.of("A"),
                result.stream().map(Plugin::id).toList()
        );
    }

    @Test
    void incompatibleVersionFails() {
        TestPlugin b = plugin("B", "1.5.0");
        TestPlugin a = plugin("A", "1.0.0", Dependency.required("B", "2.0.0"));

        assertThrows(
                PluginDependencyException.class,
                () -> resolver.resolve(List.of(a, b))
        );
    }

    @Test
    void newerVersionSatisfiesMinimum() throws Exception {
        TestPlugin b = plugin("B", "2.5.0");
        TestPlugin a = plugin("A", "1.0.0", Dependency.required("B", "2.0.0"));

        List<Plugin> result = resolver.resolve(List.of(a, b));

        assertEquals(
                List.of("B", "A"),
                result.stream().map(Plugin::id).toList()
        );
    }

    @Test
    void dependencyCycleFails() {
        TestPlugin a = plugin("A", "1.0.0", Dependency.required("B", "1.0.0"));
        TestPlugin b = plugin("B", "1.0.0", Dependency.required("A", "1.0.0"));

        PluginDependencyException exception = assertThrows(
                PluginDependencyException.class,
                () -> resolver.resolve(List.of(a, b))
        );

        String message = exception.getMessage();
        assertTrue(message.contains("A") && message.contains("B"));
    }

    @Test
    void providedDependencyDoesNotAffectStartupOrder() throws Exception {
        TestPlugin runtime = plugin("runtime", "1.0.0");
        TestPlugin a = plugin("A", "1.0.0", Dependency.provided("runtime", "1.0.0"));

        List<Plugin> result = resolver.resolve(List.of(a, runtime));

        assertEquals(
                List.of("A", "runtime"),
                result.stream().map(Plugin::id).toList()
        );
    }

    private static TestPlugin plugin(String id, String version, Dependency... dependencies) {
        return new TestPlugin(
                id,
                Version.parse(version),
                List.of(dependencies)
        );
    }

    private static final class TestPlugin implements Plugin {

        private final String id;
        private final Version version;
        private final List<Dependency> dependencies;

        private TestPlugin(String id, Version version, List<Dependency> dependencies) {
            this.id = id;
            this.version = version;
            this.dependencies = List.copyOf(dependencies);
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Manifest manifest() {
            return DefaultManifest.builder()
                    .id("00000000-0000-0000-0000-000000000001")
                    .name("test")
                    .version(version)
                    .dependencies(dependencies)
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
}
