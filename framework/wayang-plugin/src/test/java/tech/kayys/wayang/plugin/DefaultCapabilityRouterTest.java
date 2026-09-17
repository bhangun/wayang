package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityAvailability;
import tech.kayys.wayang.spi.capability.CapabilityDescriptor;
import tech.kayys.wayang.spi.capability.CapabilityHealth;
import tech.kayys.wayang.spi.capability.CapabilityProviderStatus;
import tech.kayys.wayang.spi.capability.CapabilityRoute;
import tech.kayys.wayang.spi.capability.CapabilityRoutingRequest;
import tech.kayys.wayang.spi.capability.CapabilityRoutingStrategy;
import tech.kayys.wayang.spi.capability.CapabilityType;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultCapabilityRouterTest {

    @Test
    void routesDirectToPreferredProvider() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager health = new DefaultCapabilityHealthManager(registry);
        DefaultCapabilityRouter router = new DefaultCapabilityRouter(registry, health);

        registry.register("model.text", "gpt-4", mockCapability("model.text", Map.of()));
        registry.register("model.text", "claude-3", mockCapability("model.text", Map.of()));

        CapabilityRoutingRequest req = new CapabilityRoutingRequest(
                "model.text",
                "claude-3",
                CapabilityRoutingStrategy.DIRECT,
                Map.of()
        );

        CapabilityRoute route = router.route(req);
        assertEquals("claude-3", route.providerId());
    }

    @Test
    void routesToLeastLoadedProvider() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager health = new DefaultCapabilityHealthManager(registry);
        DefaultCapabilityRouter router = new DefaultCapabilityRouter(registry, health);

        registry.register("ocr", "provider-busy", mockCapability("ocr", Map.of()));
        registry.register("ocr", "provider-idle", mockCapability("ocr", Map.of()));

        health.updateStatus(new CapabilityProviderStatus(
                "ocr", "provider-busy",
                CapabilityAvailability.AVAILABLE, CapabilityHealth.HEALTHY,
                Instant.now(), 100L, 0.9, "busy", Map.of()
        ));

        health.updateStatus(new CapabilityProviderStatus(
                "ocr", "provider-idle",
                CapabilityAvailability.AVAILABLE, CapabilityHealth.HEALTHY,
                Instant.now(), 20L, 0.1, "idle", Map.of()
        ));

        CapabilityRoutingRequest req = new CapabilityRoutingRequest(
                "ocr",
                null,
                CapabilityRoutingStrategy.LEAST_LOAD,
                Map.of()
        );

        CapabilityRoute route = router.route(req);
        assertEquals("provider-idle", route.providerId());
    }

    @Test
    void skipsUnhealthyProvider() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        DefaultCapabilityHealthManager health = new DefaultCapabilityHealthManager(registry);
        DefaultCapabilityRouter router = new DefaultCapabilityRouter(registry, health);

        registry.register("search", "search-primary", mockCapability("search", Map.of()));
        registry.register("search", "search-backup", mockCapability("search", Map.of()));

        health.updateStatus(new CapabilityProviderStatus(
                "search", "search-primary",
                CapabilityAvailability.AVAILABLE, CapabilityHealth.UNHEALTHY,
                Instant.now(), null, 1.0, "down", Map.of()
        ));

        health.updateStatus(new CapabilityProviderStatus(
                "search", "search-backup",
                CapabilityAvailability.AVAILABLE, CapabilityHealth.HEALTHY,
                Instant.now(), 50L, 0.2, "ok", Map.of()
        ));

        CapabilityRoutingRequest req = new CapabilityRoutingRequest(
                "search",
                null,
                CapabilityRoutingStrategy.FIRST_AVAILABLE,
                Map.of()
        );

        CapabilityRoute route = router.route(req);
        assertEquals("search-backup", route.providerId());
    }

    private Capability mockCapability(String id, Map<String, Object> attrs) {
        return new Capability() {
            @Override
            public String id() { return id; }
            @Override
            public CapabilityType type() { return new CapabilityType("test", "test"); }
            @Override
            public CapabilityDescriptor descriptor() {
                return new CapabilityDescriptor(id, "test", type(), attrs);
            }
        };
    }
}
