package tech.kayys.wayang.plugin;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.spi.capability.Capability;
import tech.kayys.wayang.spi.capability.CapabilityDescriptor;
import tech.kayys.wayang.spi.capability.CapabilityProviderRegistration;
import tech.kayys.wayang.spi.capability.CapabilityType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultCapabilityRegistryTest {

    @Test
    void registersAndFindsCapability() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        Capability capability = mockCapability("wayang.browser", "browser-local");

        registry.register("wayang.browser", "browser-local", capability);

        assertTrue(registry.contains("wayang.browser"));
        assertEquals(1, registry.providersOf("wayang.browser").size());

        Optional<Capability> found = registry.find("wayang.browser");
        assertTrue(found.isPresent());
        assertEquals("wayang.browser", found.get().id());
    }

    @Test
    void allowsMultipleProvidersForSameCapability() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        Capability local = mockCapability("wayang.ocr", "ocr-local");
        Capability remote = mockCapability("wayang.ocr", "ocr-cloud");

        registry.register("wayang.ocr", "ocr-local", local);
        registry.register("wayang.ocr", "ocr-cloud", remote);

        List<CapabilityProviderRegistration> providers = registry.providersOf("wayang.ocr");
        assertEquals(2, providers.size());
    }

    @Test
    void rejectsDuplicateRegistrationForSameCapabilityAndProvider() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        Capability c1 = mockCapability("wayang.git", "git-core");
        Capability c2 = mockCapability("wayang.git", "git-core");

        registry.register("wayang.git", "git-core", c1);

        assertThrows(IllegalStateException.class, () ->
                registry.register("wayang.git", "git-core", c2));
    }

    @Test
    void unregistersSpecificProvider() {
        DefaultCapabilityRegistry registry = new DefaultCapabilityRegistry();
        Capability c1 = mockCapability("wayang.git", "provider-a");
        Capability c2 = mockCapability("wayang.git", "provider-b");

        registry.register("wayang.git", "provider-a", c1);
        registry.register("wayang.git", "provider-b", c2);

        registry.unregister("wayang.git", "provider-a");

        assertEquals(1, registry.providersOf("wayang.git").size());
        assertEquals("provider-b", registry.providersOf("wayang.git").getFirst().providerId());
    }

    private Capability mockCapability(String id, String providerId) {
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
                return new CapabilityDescriptor(id, "Test capability", type(), Map.of());
            }
        };
    }
}
