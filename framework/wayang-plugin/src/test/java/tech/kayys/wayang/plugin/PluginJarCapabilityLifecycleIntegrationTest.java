package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.extension.Version;
import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityDescriptor;
import tech.kayys.wayang.spi.capability.CapabilityInvocationContext;
import tech.kayys.wayang.spi.capability.CapabilityInvocationResult;
import tech.kayys.wayang.spi.capability.CapabilityInvoker;
import tech.kayys.wayang.spi.capability.CapabilityProvider;
import tech.kayys.wayang.spi.capability.CapabilityRequirement;
import tech.kayys.wayang.spi.capability.CapabilityRequirements;
import tech.kayys.wayang.spi.capability.CapabilityResolutionResult;
import tech.kayys.wayang.spi.capability.CapabilityRoute;
import tech.kayys.wayang.spi.capability.CapabilityRoutingRequest;
import tech.kayys.wayang.spi.capability.CapabilityRoutingStrategy;
import tech.kayys.wayang.spi.capability.CapabilityType;
import tech.kayys.wayang.spi.capability.event.CapabilityEvent;
import tech.kayys.wayang.spi.capability.event.CapabilityInvocationCompletedEvent;
import tech.kayys.wayang.spi.capability.event.CapabilityRegisteredEvent;
import tech.kayys.wayang.spi.capability.event.CapabilityRoutedEvent;
import tech.kayys.wayang.spi.capability.event.CapabilityUnregisteredEvent;
import tech.kayys.wayang.spi.plugin.Manifest;
import tech.kayys.wayang.spi.plugin.Plugin;
import tech.kayys.wayang.spi.plugin.PluginState;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Full end-to-end integration test exercising:
 * 1. Physical JAR package creation
 * 2. Dynamic runtime loading via {@link RuntimePluginLoader} & {@link RuntimePluginManager}
 * 3. Lifecycle activation via {@link DefaultPluginLifecycleManager}
 * 4. Automatic capability discovery & registration via {@link PluginCapabilityDiscovery} & {@link PluginCapabilityRegistrar}
 * 5. Capability resolution via {@link DefaultCapabilityResolver}
 * 6. Health evaluation via {@link DefaultCapabilityHealthManager}
 * 7. Strategy-based routing via {@link DefaultCapabilityRouter}
 * 8. Contextual invocation via {@link CapabilityInvoker}
 * 9. Lifecycle audit events via {@link DefaultCapabilityEventPublisher}
 * 10. Clean unregistration and lifecycle teardown on plugin disable/uninstall
 */
class PluginJarCapabilityLifecycleIntegrationTest {

    @Test
    void testEndToEndJarLoadingToCapabilityExecutionAndTeardown(@TempDir Path tempDir) throws Exception {
        // 1. Prepare dynamic JAR with a Plugin implementing CapabilityProvider
        Path jarPath = tempDir.resolve("sentiment-plugin.jar");
        createJar(jarPath, SentimentAnalysisPlugin.class.getName());

        // 2. Initialize Foundation Systems
        RuntimePluginLoader loader = new RuntimePluginLoader();
        DefaultPluginRegistry pluginRegistry = new DefaultPluginRegistry();
        DefaultPluginLifecycleManager lifecycleManager = new DefaultPluginLifecycleManager(pluginRegistry);
        RuntimePluginManager pluginManager = new RuntimePluginManager(loader, pluginRegistry, lifecycleManager);

        DefaultCapabilityRegistry capabilityRegistry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager healthManager = new DefaultCapabilityHealthManager(capabilityRegistry);
        DefaultCapabilityRouter router = new DefaultCapabilityRouter(capabilityRegistry, healthManager);
        DefaultCapabilityResolver resolver = new DefaultCapabilityResolver(capabilityRegistry);
        DefaultCapabilityEventPublisher eventPublisher = new DefaultCapabilityEventPublisher();
        PluginCapabilityRegistrar capabilityRegistrar = new PluginCapabilityRegistrar(capabilityRegistry);

        List<CapabilityEvent> eventAuditLog = new ArrayList<>();
        eventPublisher.addListener(eventAuditLog::add);

        // 3. Install Plugin JAR
        Plugin plugin = pluginManager.install(jarPath);
        assertNotNull(plugin);
        assertEquals("plugin.nlp.sentiment", plugin.id());
        assertEquals(PluginState.LOADED, plugin.state());

        // 4. Enable Plugin -> Activates lifecycle
        pluginManager.enable(plugin.id());
        assertEquals(PluginState.ACTIVE, lifecycleManager.getPluginState(plugin.id()));

        // 5. Discover and Register Capabilities from the enabled plugin
        List<Capability> discoveredCapabilities = capabilityRegistrar.register(plugin);
        assertEquals(1, discoveredCapabilities.size());
        Capability sentimentCap = discoveredCapabilities.getFirst();
        assertEquals("capability.nlp.sentiment", sentimentCap.id());

        for (Capability cap : discoveredCapabilities) {
            eventPublisher.publish(new CapabilityRegisteredEvent(cap.id(), plugin.id(), cap.descriptor()));
        }

        // 6. Capability Resolution against Requirements
        CapabilityRequirements requirements = CapabilityRequirements.of(
                CapabilityRequirement.required("capability.nlp.sentiment")
        );
        CapabilityResolutionResult resolutionResult = resolver.resolve(requirements);
        assertTrue(resolutionResult.satisfied());
        assertEquals(1, resolutionResult.resolved().size());

        // 7. Health Evaluation
        healthManager.checkAll();
        var statusOpt = healthManager.statusOf("capability.nlp.sentiment", plugin.id());
        assertTrue(statusOpt.isPresent());
        assertTrue(statusOpt.get().usable());

        // 8. Route Selection
        CapabilityRoutingRequest routingRequest = new CapabilityRoutingRequest(
                "capability.nlp.sentiment",
                null,
                CapabilityRoutingStrategy.FIRST_AVAILABLE,
                Map.of()
        );
        CapabilityRoute route = router.route(routingRequest);
        assertNotNull(route);
        assertEquals(plugin.id(), route.providerId());
        assertEquals("capability.nlp.sentiment", route.capabilityId());

        eventPublisher.publish(new CapabilityRoutedEvent(
                route.capabilityId(),
                route.providerId(),
                CapabilityRoutingStrategy.FIRST_AVAILABLE,
                1
        ));

        // 9. Contextual Invocation
        CapabilityInvocationContext invocationContext = CapabilityInvocationContext.builder()
                .capabilityId(route.capabilityId())
                .providerId(route.providerId())
                .tenantId("tenant-ecommerce")
                .executionId("exec-task-9021")
                .sessionId("session-abc")
                .attribute("text", "Wayang plugin architecture is awesome!")
                .build();

        long startTime = System.currentTimeMillis();
        assertTrue(route.capability() instanceof CapabilityInvoker);
        CapabilityInvocationResult invocationResult = ((CapabilityInvoker) route.capability()).invoke(invocationContext);
        long latency = System.currentTimeMillis() - startTime;

        assertTrue(invocationResult.success());
        assertNotNull(invocationResult.output());
        @SuppressWarnings("unchecked")
        Map<String, Object> outputMap = (Map<String, Object>) invocationResult.output();
        assertEquals("POSITIVE", outputMap.get("sentiment"));
        assertEquals(0.99, (Double) outputMap.get("score"), 0.001);

        eventPublisher.publish(new CapabilityInvocationCompletedEvent(
                route.capabilityId(),
                route.providerId(),
                invocationContext.executionId().orElse("unknown"),
                latency,
                true,
                null
        ));

        // 10. Teardown: Disable plugin, unregister capabilities, uninstall JAR
        capabilityRegistrar.unregister(plugin);
        eventPublisher.publish(new CapabilityUnregisteredEvent(
                "capability.nlp.sentiment",
                plugin.id(),
                "Plugin disabled"
        ));

        pluginManager.disable(plugin.id());
        assertEquals(PluginState.STOPPED, lifecycleManager.getPluginState(plugin.id()));

        pluginManager.uninstall(plugin.id());
        assertFalse(pluginRegistry.contains(plugin.id()));
        assertFalse(capabilityRegistry.contains("capability.nlp.sentiment"));

        // 11. Verify complete event audit stream
        assertEquals(4, eventAuditLog.size());
        assertTrue(eventAuditLog.get(0) instanceof CapabilityRegisteredEvent);
        assertTrue(eventAuditLog.get(1) instanceof CapabilityRoutedEvent);
        assertTrue(eventAuditLog.get(2) instanceof CapabilityInvocationCompletedEvent);
        assertTrue(eventAuditLog.get(3) instanceof CapabilityUnregisteredEvent);
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

    public static final class SentimentAnalysisPlugin implements Plugin, CapabilityProvider {
        private final List<Capability> capabilities = new ArrayList<>();
        private PluginState state = PluginState.LOADED;

        public SentimentAnalysisPlugin() {
            capabilities.add(new SentimentAnalysisCapability());
        }

        @Override
        public String id() {
            return "plugin.nlp.sentiment";
        }

        @Override
        public Manifest manifest() {
            return DefaultManifest.builder()
                    .id("plugin.nlp.sentiment")
                    .name("Sentiment Analysis Plugin")
                    .version(Version.VERSION_1_0_0)
                    .apiVersion(Version.VERSION_1_0_0)
                    .mainClass(SentimentAnalysisPlugin.class.getName())
                    .build();
        }

        @Override
        public PluginState state() {
            return state;
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
            this.state = PluginState.LOADED;
        }

        @Override
        public void start() {
            this.state = PluginState.ACTIVE;
        }

        @Override
        public void stop() {
            this.state = PluginState.STOPPED;
        }

        @Override
        public String providerId() {
            return id();
        }

        @Override
        public List<Capability> capabilities() {
            return List.copyOf(capabilities);
        }
    }

    public static final class SentimentAnalysisCapability implements Capability, CapabilityInvoker {

        @Override
        public String id() {
            return "capability.nlp.sentiment";
        }

        @Override
        public CapabilityType type() {
            return new CapabilityType("model", "nlp");
        }

        @Override
        public CapabilityDescriptor descriptor() {
            return new CapabilityDescriptor(
                    id(),
                    "Sentiment Analysis NLP Engine",
                    type(),
                    Map.of("execution", "local", "language", "en", "priority", 10)
            );
        }

        @Override
        public CapabilityInvocationResult invoke(CapabilityInvocationContext context) throws Exception {
            String text = (String) context.attributes().get("text");
            if (text != null && (text.contains("awesome") || text.contains("great") || text.contains("good"))) {
                return CapabilityInvocationResult.success(Map.of("sentiment", "POSITIVE", "score", 0.99));
            }
            return CapabilityInvocationResult.success(Map.of("sentiment", "NEUTRAL", "score", 0.50));
        }
    }
}
