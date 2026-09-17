package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityAvailability;
import tech.kayys.wayang.spi.capability.CapabilityDescriptor;
import tech.kayys.wayang.spi.capability.CapabilityHealth;
import tech.kayys.wayang.spi.capability.CapabilityInvocationContext;
import tech.kayys.wayang.spi.capability.CapabilityInvocationResult;
import tech.kayys.wayang.spi.capability.CapabilityInvoker;
import tech.kayys.wayang.spi.capability.CapabilityProvider;
import tech.kayys.wayang.spi.capability.CapabilityProviderRegistration;
import tech.kayys.wayang.spi.capability.CapabilityProviderStatus;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapabilityChainIntegrationTest {

    @Test
    void fullCapabilityLifecycleAndEventChain() throws Exception {
        // 1. Initialize Capability Foundation components
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager healthManager = new DefaultCapabilityHealthManager(registry);
        DefaultCapabilityRouter router = new DefaultCapabilityRouter(registry, healthManager);
        DefaultCapabilityEventPublisher eventPublisher = new DefaultCapabilityEventPublisher();
        DefaultCapabilityResolver resolver = new DefaultCapabilityResolver(registry);

        List<CapabilityEvent> eventStream = new ArrayList<>();
        eventPublisher.addListener(eventStream::add);

        // 2. Discover and register capabilities from a simulated plugin
        TestPlugin webPlugin = new TestPlugin("plugin.web.browser");
        Capability browserCap = new ExecutableTestCapability(
                "wayang.browser",
                new CapabilityType("tool", "browser"),
                Map.of("execution", "local", "priority", 10),
                ctx -> CapabilityInvocationResult.success(Map.of("url", ctx.attributes().get("url"), "status", 200))
        );
        webPlugin.addCapability(browserCap);

        PluginCapabilityRegistrar registrar = new PluginCapabilityRegistrar(registry);
        List<Capability> registered = registrar.register(webPlugin);
        assertEquals(1, registered.size());

        for (Capability c : registered) {
            eventPublisher.publish(new CapabilityRegisteredEvent(c.id(), webPlugin.id(), c.descriptor()));
        }

        assertEquals(1, eventStream.size());
        assertTrue(eventStream.getFirst() instanceof CapabilityRegisteredEvent);

        // 3. Verify Capability Discovery and Resolution
        CapabilityRequirements requirements = CapabilityRequirements.of(
                CapabilityRequirement.required("wayang.browser")
        );
        CapabilityResolutionResult resolution = resolver.resolve(requirements);
        assertTrue(resolution.satisfied());
        assertNotNull(resolution.resolved().get("wayang.browser"));

        // 4. Initial health check evaluation
        healthManager.checkAll();
        assertTrue(healthManager.statusOf("wayang.browser", "plugin.web.browser").isPresent());
        assertTrue(healthManager.statusOf("wayang.browser", "plugin.web.browser").get().usable());

        // 5. Route selection
        CapabilityRoutingRequest routingReq = new CapabilityRoutingRequest(
                "wayang.browser",
                null,
                CapabilityRoutingStrategy.FIRST_AVAILABLE,
                Map.of()
        );
        CapabilityRoute route = router.route(routingReq);
        assertEquals("plugin.web.browser", route.providerId());
        assertEquals("wayang.browser", route.capabilityId());

        eventPublisher.publish(new CapabilityRoutedEvent(
                route.capabilityId(),
                route.providerId(),
                CapabilityRoutingStrategy.FIRST_AVAILABLE,
                1
        ));

        // 6. Invocation execution with tenant and execution scope
        CapabilityInvocationContext context = CapabilityInvocationContext.builder()
                .capabilityId(route.capabilityId())
                .providerId(route.providerId())
                .tenantId("tenant-corp-a")
                .executionId("exec-001")
                .sessionId("session-xyz")
                .attribute("url", "https://wayang.tech")
                .build();

        long startTime = System.currentTimeMillis();
        ExecutableTestCapability executable = (ExecutableTestCapability) route.capability();
        CapabilityInvocationResult result = executable.invoke(context);
        long latency = System.currentTimeMillis() - startTime;

        assertTrue(result.success());
        assertNotNull(result.output());

        eventPublisher.publish(new CapabilityInvocationCompletedEvent(
                route.capabilityId(),
                route.providerId(),
                context.executionId().orElse("unknown"),
                latency,
                true,
                null
        ));

        // 7. Graceful Plugin Disabling and Cascading Unregistration
        registrar.unregister(webPlugin);
        assertFalse(registry.contains("wayang.browser"));
        assertTrue(registry.providersOf("wayang.browser").isEmpty());

        eventPublisher.publish(new CapabilityUnregisteredEvent(
                "wayang.browser",
                webPlugin.id(),
                "Plugin disabled"
        ));

        assertEquals(4, eventStream.size());
        assertTrue(eventStream.get(0) instanceof CapabilityRegisteredEvent);
        assertTrue(eventStream.get(1) instanceof CapabilityRoutedEvent);
        assertTrue(eventStream.get(2) instanceof CapabilityInvocationCompletedEvent);
        assertTrue(eventStream.get(3) instanceof CapabilityUnregisteredEvent);
    }

    @Test
    void multiProviderFailoverAndLeastLoadRouting() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager healthManager = new DefaultCapabilityHealthManager(registry);
        DefaultCapabilityRouter router = new DefaultCapabilityRouter(registry, healthManager);

        Capability localOcr = new ExecutableTestCapability(
                "wayang.ocr",
                new CapabilityType("model", "ocr"),
                Map.of("execution", "local", "priority", 1),
                ctx -> CapabilityInvocationResult.success("local-ocr-text")
        );

        Capability cloudOcr = new ExecutableTestCapability(
                "wayang.ocr",
                new CapabilityType("model", "ocr"),
                Map.of("execution", "remote", "priority", 2),
                ctx -> CapabilityInvocationResult.success("cloud-ocr-text")
        );

        registry.register("wayang.ocr", "provider.local", localOcr);
        registry.register("wayang.ocr", "provider.cloud", cloudOcr);

        // 1. Initial routing via LEAST_LOAD: set provider.local busy (0.8 load) and provider.cloud idle (0.1 load)
        healthManager.updateStatus(new CapabilityProviderStatus(
                "wayang.ocr", "provider.local",
                CapabilityAvailability.AVAILABLE, CapabilityHealth.HEALTHY,
                Instant.now(), 50L, 0.8, "busy", Map.of()
        ));
        healthManager.updateStatus(new CapabilityProviderStatus(
                "wayang.ocr", "provider.cloud",
                CapabilityAvailability.AVAILABLE, CapabilityHealth.HEALTHY,
                Instant.now(), 150L, 0.1, "idle", Map.of()
        ));

        CapabilityRoute route1 = router.route(new CapabilityRoutingRequest(
                "wayang.ocr", null, CapabilityRoutingStrategy.LEAST_LOAD, Map.of()
        ));
        assertEquals("provider.cloud", route1.providerId());

        // 2. Rebalance: provider.cloud becomes overloaded (0.95 load)
        healthManager.updateStatus(new CapabilityProviderStatus(
                "wayang.ocr", "provider.cloud",
                CapabilityAvailability.AVAILABLE, CapabilityHealth.HEALTHY,
                Instant.now(), 200L, 0.95, "overloaded", Map.of()
        ));

        CapabilityRoute route2 = router.route(new CapabilityRoutingRequest(
                "wayang.ocr", null, CapabilityRoutingStrategy.LEAST_LOAD, Map.of()
        ));
        assertEquals("provider.local", route2.providerId());

        // 3. Failover: provider.local crashes and becomes UNHEALTHY
        healthManager.updateStatus(new CapabilityProviderStatus(
                "wayang.ocr", "provider.local",
                CapabilityAvailability.AVAILABLE, CapabilityHealth.UNHEALTHY,
                Instant.now(), null, 1.0, "connection refused", Map.of()
        ));

        // Even though cloud has high load, local is UNHEALTHY so router routes to cloud
        CapabilityRoute route3 = router.route(new CapabilityRoutingRequest(
                "wayang.ocr", null, CapabilityRoutingStrategy.LEAST_LOAD, Map.of()
        ));
        assertEquals("provider.cloud", route3.providerId());
    }

    @Test
    void circuitBreakerConsecutiveFailuresAndRecovery() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager healthManager = new DefaultCapabilityHealthManager(registry);

        Capability cap = new ExecutableTestCapability(
                "model.translate",
                new CapabilityType("model", "nlp"),
                Map.of(),
                ctx -> CapabilityInvocationResult.success("translated")
        );
        registry.register("model.translate", "provider.translate", cap);

        // Initial state: Healthy
        healthManager.check(new CapabilityProviderRegistration("model.translate", "provider.translate", cap));
        assertEquals(CapabilityHealth.HEALTHY,
                healthManager.statusOf("model.translate", "provider.translate").orElseThrow().health());

        // Record 3 failures -> Transitions to DEGRADED
        healthManager.recordFailure("model.translate", "provider.translate", new RuntimeException("timeout 1"));
        healthManager.recordFailure("model.translate", "provider.translate", new RuntimeException("timeout 2"));
        healthManager.recordFailure("model.translate", "provider.translate", new RuntimeException("timeout 3"));

        assertEquals(CapabilityHealth.DEGRADED,
                healthManager.statusOf("model.translate", "provider.translate").orElseThrow().health());

        // Record 2 more failures (total 5) -> Transitions to UNHEALTHY (circuit tripped)
        healthManager.recordFailure("model.translate", "provider.translate", new RuntimeException("timeout 4"));
        healthManager.recordFailure("model.translate", "provider.translate", new RuntimeException("timeout 5"));

        assertEquals(CapabilityHealth.UNHEALTHY,
                healthManager.statusOf("model.translate", "provider.translate").orElseThrow().health());
        assertFalse(healthManager.statusOf("model.translate", "provider.translate").orElseThrow().usable());

        // Success probe -> Recovers to HEALTHY
        healthManager.recordSuccess("model.translate", "provider.translate", 25L);
        assertEquals(CapabilityHealth.HEALTHY,
                healthManager.statusOf("model.translate", "provider.translate").orElseThrow().health());
        assertTrue(healthManager.statusOf("model.translate", "provider.translate").orElseThrow().usable());
    }

    @Test
    void concurrentResolutionAndRoutingUnderLoad() throws Exception {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager healthManager = new DefaultCapabilityHealthManager(registry);
        DefaultCapabilityRouter router = new DefaultCapabilityRouter(registry, healthManager);
        DefaultCapabilityResolver resolver = new DefaultCapabilityResolver(registry);

        for (int i = 0; i < 5; i++) {
            final String pid = "provider-" + i;
            Capability c = new ExecutableTestCapability(
                    "service.calculator",
                    new CapabilityType("compute", "math"),
                    Map.of("priority", i),
                    ctx -> CapabilityInvocationResult.success("calc-ok")
            );
            registry.register("service.calculator", pid, c);
            healthManager.updateStatus(new CapabilityProviderStatus(
                    "service.calculator", pid,
                    CapabilityAvailability.AVAILABLE, CapabilityHealth.HEALTHY,
                    Instant.now(), 10L, 0.1 * i, "ok", Map.of()
            ));
        }

        int threads = 10;
        int operationsPerThread = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        AtomicInteger successfulRoutings = new AtomicInteger();

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                try {
                    for (int op = 0; op < operationsPerThread; op++) {
                        CapabilityResolutionResult res = resolver.resolve(
                                CapabilityRequirements.of(CapabilityRequirement.required("service.calculator"))
                        );
                        if (res.satisfied()) {
                            CapabilityRoute route = router.route(new CapabilityRoutingRequest(
                                    "service.calculator",
                                    null,
                                    CapabilityRoutingStrategy.LEAST_LOAD,
                                    Map.of()
                            ));
                            if (route != null && "provider-0".equals(route.providerId())) {
                                successfulRoutings.incrementAndGet();
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(threads * operationsPerThread, successfulRoutings.get());
    }

    // --- Test Fixtures ---

    private static final class TestPlugin implements tech.kayys.wayang.spi.plugin.Plugin, CapabilityProvider {
        private final String id;
        private final List<Capability> capabilities = new ArrayList<>();

        private TestPlugin(String id) {
            this.id = id;
        }

        public void addCapability(Capability capability) {
            capabilities.add(capability);
        }

        @Override
        public String id() { return id; }

        @Override
        public tech.kayys.wayang.spi.plugin.Manifest manifest() { return null; }

        @Override
        public tech.kayys.wayang.spi.plugin.PluginState state() { return tech.kayys.wayang.spi.plugin.PluginState.ACTIVE; }

        @Override
        public ClassLoader classLoader() { return getClass().getClassLoader(); }

        @Override
        public List<tech.kayys.wayang.extension.Extension> extensions() { return List.of(); }

        @Override
        public void initialize() {}

        @Override
        public void start() {}

        @Override
        public void stop() {}

        @Override
        public String providerId() { return id; }

        @Override
        public List<Capability> capabilities() { return List.copyOf(capabilities); }
    }

    private static final class ExecutableTestCapability implements Capability, CapabilityInvoker {
        private final String id;
        private final CapabilityType type;
        private final Map<String, Object> attributes;
        private final CapabilityInvoker invoker;

        private ExecutableTestCapability(
                String id,
                CapabilityType type,
                Map<String, Object> attributes,
                CapabilityInvoker invoker) {
            this.id = id;
            this.type = type;
            this.attributes = Map.copyOf(attributes);
            this.invoker = invoker;
        }

        @Override
        public String id() { return id; }

        @Override
        public CapabilityType type() { return type; }

        @Override
        public CapabilityDescriptor descriptor() {
            return new CapabilityDescriptor(id, "Test description", type, attributes);
        }

        @Override
        public CapabilityInvocationResult invoke(CapabilityInvocationContext context) throws Exception {
            return invoker.invoke(context);
        }
    }
}
