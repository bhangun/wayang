package tech.kayys.wayang.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginState;

class DefaultPluginRegistryTest {

    @Test
    void registersAndFindsPlugin() {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();
        Plugin plugin = plugin("test.plugin");

        registry.register(plugin);

        assertTrue(registry.getPlugin("test.plugin").isPresent());
        assertEquals(plugin, registry.getPlugin("test.plugin").orElseThrow());
    }

    @Test
    void rejectsDuplicatePluginId() {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();

        registry.register(plugin("test.plugin"));

        assertThrows(
                IllegalStateException.class,
                () -> registry.register(plugin("test.plugin"))
        );
    }

    @Test
    void unregistersPlugin() {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();

        registry.register(plugin("test.plugin"));

        assertTrue(registry.contains("test.plugin"));
        assertTrue(registry.unregister("test.plugin").isPresent());
        assertFalse(registry.contains("test.plugin"));
    }

    @Test
    void returnsEmptyForUnknownPlugin() {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();

        assertTrue(registry.getPlugin("missing").isEmpty());
    }

    @Test
    void returnsAllPlugins() {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();

        registry.register(plugin("plugin.a"));
        registry.register(plugin("plugin.b"));

        assertEquals(2, registry.getPlugins().size());
    }

    private Plugin plugin(String id) {
        return new Plugin() {

            @Override
            public String id() {
                return id;
            }

            @Override
            public tech.kayys.wayang.spi.plugin.Manifest manifest() {
                throw new UnsupportedOperationException();
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
        };
    }
}
