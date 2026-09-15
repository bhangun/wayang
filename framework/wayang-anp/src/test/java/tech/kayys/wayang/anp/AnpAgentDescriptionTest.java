package tech.kayys.wayang.anp;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.anp.description.AnpAgentDescription;
import tech.kayys.wayang.anp.description.AnpCapabilityDescriptor;
import tech.kayys.wayang.anp.meta.AnpCapabilityAdvertisement;
import tech.kayys.wayang.anp.meta.AnpCapabilitySelection;
import tech.kayys.wayang.anp.meta.AnpMetaProtocolNegotiator;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnpAgentDescriptionTest {

    @Test
    void descriptionBuilderConstructsValidDoc() {
        AnpAgentDescription desc = AnpAgentDescription.builder("did:wba:example.com:coder", "Code Assistant")
                .description("Autonomous coding agent")
                .version("1.1.0")
                .protocols(List.of("anp/1.1", "a2a/1.0"))
                .capabilities(List.of(
                        AnpCapabilityDescriptor.of("code.generate", "Generate Code", "Generates Java code"),
                        AnpCapabilityDescriptor.of("git.commit", "Git Commit", "Commits changes")
                ))
                .endpoints(Map.of("messaging", "https://example.com/message"))
                .build();

        assertEquals("did:wba:example.com:coder", desc.did());
        assertEquals("Code Assistant", desc.name());
        assertTrue(desc.supportsProtocol("anp/1.1"));
        assertTrue(desc.supportsProtocol("a2a/1.0"));
        assertTrue(desc.hasCapability("code.generate"));
        assertTrue(desc.hasCapability("git.commit"));
        assertFalse(desc.hasCapability("unknown.cap"));
    }

    @Test
    void metaProtocolNegotiatorSelectsA2AWhenBothSupportIt() {
        AnpMetaProtocolNegotiator negotiator = new AnpMetaProtocolNegotiator();

        // Initiator (Wayang) prefers A2A for max interop
        AnpCapabilityAdvertisement wayang = AnpCapabilityAdvertisement.defaultWayang("did:wba:wayang.local:agent1");

        // External ANP agent also supports A2A
        AnpCapabilityAdvertisement external = new AnpCapabilityAdvertisement(
                "did:wba:partner.com:bot",
                List.of("a2a/1.0", "anp/1.1"),
                List.of("application/json"),
                Map.of()
        );

        AnpCapabilitySelection selected = negotiator.negotiate(wayang, external);

        assertEquals("a2a/1.0", selected.protocol());
        assertTrue(selected.isA2A());
        assertEquals("application/json", selected.codec());
    }

    @Test
    void metaProtocolNegotiatorFallsBackToAnpWhenNoA2A() {
        AnpMetaProtocolNegotiator negotiator = new AnpMetaProtocolNegotiator();

        AnpCapabilityAdvertisement wayang = AnpCapabilityAdvertisement.defaultWayang("did:wba:wayang.local:agent1");

        // External agent only supports anp/1.1
        AnpCapabilityAdvertisement external = new AnpCapabilityAdvertisement(
                "did:wba:strict-anp.com:agent",
                List.of("anp/1.1"),
                List.of("application/json"),
                Map.of()
        );

        AnpCapabilitySelection selected = negotiator.negotiate(wayang, external);

        assertEquals("anp/1.1", selected.protocol());
        assertFalse(selected.isA2A());
    }
}
