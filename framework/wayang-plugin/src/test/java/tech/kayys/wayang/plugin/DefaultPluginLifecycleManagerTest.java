package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;

import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginContext;
import tech.kayys.wayang.spi.plugin.PluginState;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class DefaultPluginLifecycleManagerTest {

    @Test
    void enablePluginRunsInitializeAndStart() throws Exception {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();
        TestPlugin plugin = new TestPlugin("test-plugin");
        registry.register(plugin);

        DefaultPluginLifecycleManager lifecycle = new DefaultPluginLifecycleManager(registry);
        lifecycle.enablePlugin("test-plugin");

        assertEquals(PluginState.ACTIVE, lifecycle.getPluginState("test-plugin"));
        assertEquals(1, plugin.initializeCount.get());
        assertEquals(1, plugin.startCount.get());
    }

    @Test
    void enableActivePluginIsIdempotent() throws Exception {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();
        TestPlugin plugin = new TestPlugin("test-plugin");
        registry.register(plugin);

        DefaultPluginLifecycleManager lifecycle = new DefaultPluginLifecycleManager(registry);
        lifecycle.enablePlugin("test-plugin");
        lifecycle.enablePlugin("test-plugin");

        assertEquals(1, plugin.initializeCount.get());
        assertEquals(1, plugin.startCount.get());
    }

    @Test
    void disablePluginStopsActivePlugin() throws Exception {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();
        TestPlugin plugin = new TestPlugin("test-plugin");
        registry.register(plugin);

        DefaultPluginLifecycleManager lifecycle = new DefaultPluginLifecycleManager(registry);
        lifecycle.enablePlugin("test-plugin");
        lifecycle.disablePlugin("test-plugin");

        assertEquals(PluginState.STOPPED, lifecycle.getPluginState("test-plugin"));
        assertEquals(1, plugin.stopCount.get());
    }

    @Test
    void initializeFailureMovesPluginToError() {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();
        TestPlugin plugin = new TestPlugin("test-plugin");
        plugin.failInitialize = true;
        registry.register(plugin);

        DefaultPluginLifecycleManager lifecycle = new DefaultPluginLifecycleManager(registry);

        assertThrows(
                PluginLifecycleException.class,
                () -> lifecycle.enablePlugin("test-plugin")
        );

        assertEquals(PluginState.ERROR, lifecycle.getPluginState("test-plugin"));
    }

    @Test
    void startupOrderIsReversedDuringShutdown() throws Exception {
        DefaultPluginRegistry registry = new DefaultPluginRegistry();
        TestPlugin first = new TestPlugin("first");
        TestPlugin second = new TestPlugin("second");

        registry.register(first);
        registry.register(second);

        DefaultPluginLifecycleManager lifecycle = new DefaultPluginLifecycleManager(registry);
        lifecycle.enablePlugin("first");
        lifecycle.enablePlugin("second");

        lifecycle.disableAll();

        assertEquals(PluginState.STOPPED, lifecycle.getPluginState("first"));
        assertEquals(PluginState.STOPPED, lifecycle.getPluginState("second"));
    }

    private static final class TestPlugin implements Plugin {

        private final String id;
        private final AtomicInteger initializeCount = new AtomicInteger();
        private final AtomicInteger startCount = new AtomicInteger();
        private final AtomicInteger stopCount = new AtomicInteger();
        private boolean failInitialize;

        private TestPlugin(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Manifest manifest() {
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
            if (failInitialize) {
                throw new IllegalStateException("Initialization failed");
            }
            initializeCount.incrementAndGet();
        }

        @Override
        public void initialize(PluginContext context) throws Exception {
            initialize();
        }

        @Override
        public void start() {
            startCount.incrementAndGet();
        }

        @Override
        public void stop() {
            stopCount.incrementAndGet();
        }
    }
}
