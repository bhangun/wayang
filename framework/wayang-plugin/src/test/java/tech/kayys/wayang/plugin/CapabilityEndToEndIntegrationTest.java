package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityDescriptor;
import tech.kayys.wayang.spi.capability.CapabilityInvocationContext;
import tech.kayys.wayang.spi.capability.CapabilityRoute;
import tech.kayys.wayang.spi.capability.CapabilityRoutingRequest;
import tech.kayys.wayang.spi.capability.CapabilityRoutingStrategy;
import tech.kayys.wayang.spi.capability.CapabilityType;
import tech.kayys.wayang.spi.capability.event.CapabilityEvent;
import tech.kayys.wayang.spi.capability.event.CapabilityRegisteredEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CapabilityEndToEndIntegrationTest {

    @Test
    void fullCapabilityLifecycleFlow() throws Exception {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager health = new DefaultCapabilityHealthManager(registry);
        DefaultCapabilityRouter router = new DefaultCapabilityRouter(registry, health);
        DefaultCapabilityEventPublisher eventPublisher = new DefaultCapabilityEventPublisher();

        List<CapabilityEvent> capturedEvents = new ArrayList<>();
        eventPublisher.addListener(capturedEvents::add);

        Capability webSearch = new MockSearchCapability("capability.search");
        registry.register("capability.search", "google-provider", webSearch);
        eventPublisher.publish(new CapabilityRegisteredEvent("capability.search", "google-provider", webSearch.descriptor()));

        assertEquals(1, capturedEvents.size());
        assertTrue(capturedEvents.getFirst() instanceof CapabilityRegisteredEvent);

        health.checkAll();
        assertTrue(health.statusOf("capability.search", "google-provider").isPresent());
        assertTrue(health.statusOf("capability.search", "google-provider").get().healthy());

        CapabilityRoutingRequest routingReq = new CapabilityRoutingRequest(
                "capability.search",
                null,
                CapabilityRoutingStrategy.FIRST_AVAILABLE,
                Map.of()
        );
        CapabilityRoute route = router.route(routingReq);
        assertEquals("google-provider", route.providerId());

        CapabilityInvocationContext context = CapabilityInvocationContext.builder()
                .capabilityId(route.capabilityId())
                .providerId(route.providerId())
                .tenantId("tenant-123")
                .executionId("exec-abc")
                .attribute("query", "Wayang agents")
                .build();

        assertNotNull(context);
        assertEquals("tenant-123", context.tenantId().orElseThrow());
        assertEquals("Wayang agents", context.attributes().get("query"));
    }

    private static final class MockSearchCapability implements Capability {
        private final String id;

        private MockSearchCapability(String id) {
            this.id = id;
        }

        @Override
        public String id() { return id; }

        @Override
        public CapabilityType type() { return new CapabilityType("search", "web"); }

        @Override
        public CapabilityDescriptor descriptor() {
            return new CapabilityDescriptor(id, "Web search", type(), Map.of("execution", "local"));
        }
    }
}
