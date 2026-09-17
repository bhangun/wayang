package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityDescriptor;
import tech.kayys.wayang.spi.capability.CapabilityRequirement;
import tech.kayys.wayang.spi.capability.CapabilityRequirements;
import tech.kayys.wayang.spi.capability.CapabilityResolutionResult;
import tech.kayys.wayang.spi.capability.CapabilityType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCapabilityResolverTest {

    @Test
    void resolvesRequiredCapabilities() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        registry.register("wayang.browser", "default", mockCapability("wayang.browser", Map.of()));
        registry.register("wayang.git", "default", mockCapability("wayang.git", Map.of()));

        DefaultCapabilityResolver resolver = new DefaultCapabilityResolver(registry);

        CapabilityRequirements reqs = new CapabilityRequirements(List.of(
                CapabilityRequirement.required("wayang.browser"),
                CapabilityRequirement.required("wayang.git")
        ));

        CapabilityResolutionResult result = resolver.resolve(reqs);

        assertTrue(result.satisfied());
        assertEquals(2, result.resolved().size());
    }

    @Test
    void failsWhenRequiredCapabilityIsMissing() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        registry.register("wayang.browser", "default", mockCapability("wayang.browser", Map.of()));

        DefaultCapabilityResolver resolver = new DefaultCapabilityResolver(registry);

        CapabilityRequirements reqs = new CapabilityRequirements(List.of(
                CapabilityRequirement.required("wayang.browser"),
                CapabilityRequirement.required("wayang.missing")
        ));

        CapabilityResolutionResult result = resolver.resolve(reqs);

        assertFalse(result.satisfied());
        assertTrue(result.missing().contains("wayang.missing"));
    }

    @Test
    void succeedsWhenOptionalCapabilityIsMissing() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        registry.register("wayang.browser", "default", mockCapability("wayang.browser", Map.of()));

        DefaultCapabilityResolver resolver = new DefaultCapabilityResolver(registry);

        CapabilityRequirements reqs = new CapabilityRequirements(List.of(
                CapabilityRequirement.required("wayang.browser"),
                CapabilityRequirement.optional("wayang.optional-feature")
        ));

        CapabilityResolutionResult result = resolver.resolve(reqs);

        assertTrue(result.satisfied());
        assertEquals(1, result.resolved().size());
    }

    private Capability mockCapability(String id, Map<String, Object> attributes) {
        return new Capability() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public CapabilityType type() {
                return new CapabilityType("tool", "system");
            }

            @Override
            public CapabilityDescriptor descriptor() {
                return new CapabilityDescriptor(id, "Test", type(), attributes);
            }
        };
    }
}
