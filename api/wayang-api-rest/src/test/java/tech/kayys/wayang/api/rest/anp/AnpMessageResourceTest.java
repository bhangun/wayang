package tech.kayys.wayang.api.rest.anp;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.anp.auth.AnpAuthenticator;
import tech.kayys.wayang.anp.auth.AnpHttpSignature;
import tech.kayys.wayang.anp.config.AnpProtocolConfig;
import tech.kayys.wayang.anp.identity.AnpIdentityKeyPair;
import tech.kayys.wayang.anp.identity.AnpKeyStore;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnpMessageResourceTest {

    private AnpMessageResource resource;
    private AnpProducer producer;
    private AnpAuthenticator authenticator;
    private AnpKeyStore keyStore;
    private AnpIdentityKeyPair clientKey;

    @BeforeEach
    void setUp() {
        producer = new AnpProducer();
        AnpProtocolConfig config = producer.produceConfig();

        resource = new AnpMessageResource();
        resource.config = config;
        keyStore = producer.produceKeyStore(config);
        resource.keyStore = keyStore;
        authenticator = producer.produceAuthenticator();
        resource.authenticator = authenticator;
        resource.authVerifier = producer.produceAuthVerifier(keyStore);
        var principalResolver = producer.producePrincipalResolver();
        resource.securityMapper = producer.produceSecurityMapper(principalResolver);

        resource.wellKnownResource = new AnpWellKnownResource();
        resource.wellKnownResource.config = config;
        resource.wellKnownResource.descriptionService = producer.produceDescriptionService(config);
        resource.wellKnownResource.didResolver = producer.produceDidResolver(config);

        // Register client key for signature verification
        byte[] secret = "test-client-secret-key-32bytes!".getBytes(StandardCharsets.UTF_8);
        clientKey = new AnpIdentityKeyPair("client-key-1", "hmac-sha256", secret, secret);
        keyStore.storeKey("client-agent", clientKey);
    }

    @Test
    void acceptsMessageAndSignsResponse() {
        AnpMessageResource.AnpMessageRequest request = new AnpMessageResource.AnpMessageRequest(
                "did:wba:external.org:agent-client",
                "did:wba:test.wayang.local:wayang",
                "a2a/1.0",
                "Hello Wayang Agent",
                "tenant-1",
                Map.of()
        );

        // Sign with client key
        AnpHttpSignature sig = authenticator.sign("POST", "/api/v1/anp/message", "test.wayang.local", clientKey);
        String sigHeader = "Signature " + sig.toHeaderValue();

        Response response = resource.handleMessage(sigHeader, null, "test.wayang.local", request);

        assertEquals(200, response.getStatus());
        assertNotNull(response.getEntity());
        assertNotNull(response.getHeaderString("Signature"));

        AnpMessageResource.AnpMessageResponse entity = (AnpMessageResource.AnpMessageResponse) response.getEntity();
        assertTrue(entity.success());
        assertTrue(entity.payload().contains("Hello Wayang Agent"));
    }

    @Test
    void rejectsMessageWithMissingSender() {
        AnpMessageResource.AnpMessageRequest request = new AnpMessageResource.AnpMessageRequest(
                "",
                "did:wba:test.wayang.local:wayang",
                "a2a/1.0",
                "Test",
                null,
                Map.of()
        );

        Response response = resource.handleMessage(null, null, null, request);
        assertEquals(400, response.getStatus());
    }

    @Test
    void statusEndpointReturnsProtocolDetails() {
        Response response = resource.status();
        assertEquals(200, response.getStatus());

        Map<?, ?> map = (Map<?, ?>) response.getEntity();
        assertEquals("ANP", map.get("protocol"));
        assertEquals("1.1", map.get("version"));
        assertEquals(true, map.get("enabled"));
    }
}
