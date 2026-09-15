package tech.kayys.wayang.anp;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.anp.identity.DidWbaIdentity;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DidWbaIdentityTest {

    @Test
    void parsesSimpleDomain() {
        DidWbaIdentity id = DidWbaIdentity.parse("did:wba:example.com");
        assertEquals("example.com", id.host());
        assertEquals(-1, id.port());
        assertTrue(id.pathSegments().isEmpty());
        assertEquals(URI.create("https://example.com/.well-known/did.json"), id.toDidDocumentUrl());
        assertEquals(URI.create("https://example.com/.well-known/agent.json"), id.toAgentDescriptionUrl());
    }

    @Test
    void parsesDomainWithPortAndPathSegments() {
        DidWbaIdentity id = DidWbaIdentity.parse("did:wba:localhost:8443:agents:finance:advisor");
        assertEquals("localhost", id.host());
        assertEquals(8443, id.port());
        assertEquals(List.of("agents", "finance", "advisor"), id.pathSegments());
        assertEquals(URI.create("https://localhost:8443/agents/finance/advisor/did.json"), id.toDidDocumentUrl());
        assertEquals(URI.create("https://localhost:8443/.well-known/agent.json"), id.toAgentDescriptionUrl());
    }

    @Test
    void parsesDomainWithAgentName() {
        DidWbaIdentity id = DidWbaIdentity.of("agents.kayys.tech", "support");
        assertEquals("did:wba:agents.kayys.tech:support", id.raw());
        assertEquals("agents.kayys.tech", id.host());
        assertEquals(List.of("support"), id.pathSegments());
    }

    @Test
    void rejectsInvalidScheme() {
        assertThrows(IllegalArgumentException.class, () -> DidWbaIdentity.parse("did:web:example.com"));
        assertThrows(IllegalArgumentException.class, () -> DidWbaIdentity.parse("not-a-did"));
    }
}
