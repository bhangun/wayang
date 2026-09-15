package tech.kayys.wayang.a2a.descriptor;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.communication.api.AgentCapability;
import tech.kayys.wayang.communication.api.AgentDescriptor;
import tech.kayys.wayang.communication.api.AgentEndpointDescriptor;
import tech.kayys.wayang.communication.api.AgentRef;
import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.endpoint.RemoteAgentEndpoint;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class A2AAgentCardMapperTest {

    @Test
    void testMapCanonicalDescriptorToAgentCard() {
        AgentRef ref = AgentRef.remote("coder-agent", "Coder Agent", URI.create("https://agent.example.com/a2a"));
        AgentCapability cap1 = new AgentCapability(CapabilityId.of("coding.review"), "Code review capability", Map.of());
        AgentCapability cap2 = new AgentCapability(CapabilityId.of("coding.refactor"), "Refactoring capability", Map.of());

        AgentEndpointDescriptor ep1 = new AgentEndpointDescriptor(
                RemoteAgentEndpoint.of(URI.create("https://agent.example.com/a2a")),
                "HTTP+JSON",
                "1.0",
                Map.of()
        );
        AgentEndpointDescriptor ep2 = new AgentEndpointDescriptor(
                RemoteAgentEndpoint.of(URI.create("https://agent.example.com/grpc")),
                "GRPC",
                "1.0",
                Map.of()
        );

        AgentDescriptor descriptor = new AgentDescriptor(
                ref,
                "A smart coding agent",
                List.of(cap1, cap2),
                List.of(ep1, ep2),
                Map.of("version", "2.1.0", "internalSecret", "shh")
        );

        // Test without internal metadata exposure
        A2AAgentCard card = A2AAgentCardMapper.fromWayang(descriptor, null);

        assertNotNull(card);
        assertEquals("1.0", card.protocolVersion());
        assertEquals("Coder Agent", card.name());
        assertEquals("A smart coding agent", card.description());
        assertEquals("2.1.0", card.version());

        assertEquals(2, card.supportedInterfaces().size());
        assertEquals("https://agent.example.com/a2a", card.supportedInterfaces().get(0).url());
        assertEquals("HTTP+JSON", card.supportedInterfaces().get(0).protocolBinding());
        assertEquals("https://agent.example.com/grpc", card.supportedInterfaces().get(1).url());
        assertEquals("GRPC", card.supportedInterfaces().get(1).protocolBinding());

        assertEquals(2, card.skills().size());
        assertEquals("coding.review", card.skills().get(0).id());
        assertEquals("coding.refactor", card.skills().get(1).id());

        assertTrue(card.capabilities().streaming());
        assertTrue(card.capabilities().pushNotifications());

        // Internal metadata should NOT be exposed by default
        assertTrue(card.metadata().isEmpty());

        // Now test with metadata enabled
        A2AAgentCard cardWithMeta = A2AAgentCardMapper.fromWayang(descriptor, null, new DefaultA2ACapabilityMapper(), true);
        assertEquals("shh", cardWithMeta.metadata().get("internalSecret"));
    }

    @Test
    void testA2AAgentCardServiceGeneratesCard() {
        AgentRef ref = AgentRef.local("analyst", "Analyst", "runtime-1");
        AgentDescriptor descriptor = AgentDescriptor.of(ref, "Data analyst");

        A2AAgentCardService service = new A2AAgentCardService(descriptor, "https://service.local/a2a");
        A2AAgentCard card = service.card();

        assertNotNull(card);
        assertEquals("Analyst", card.name());
        assertEquals(1, card.supportedInterfaces().size());
        assertEquals("https://service.local/a2a", card.supportedInterfaces().getFirst().url());
    }
}
