package tech.kayys.wayang.anp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.anp.auth.AnpAuthenticator;
import tech.kayys.wayang.anp.client.AnpClient;
import tech.kayys.wayang.anp.client.AnpNetworkClient;
import tech.kayys.wayang.anp.client.AnpNetworkProtocol;
import tech.kayys.wayang.anp.config.AnpProtocolConfig;
import tech.kayys.wayang.anp.identity.AnpIdentityKeyPair;
import tech.kayys.wayang.anp.identity.InMemoryAnpKeyStore;
import tech.kayys.wayang.anp.meta.AnpMetaProtocolNegotiator;
import tech.kayys.wayang.communication.api.AgentRef;
import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.network.AgentNetworkRequest;
import tech.kayys.wayang.network.AgentNetworkResponse;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AnpNetworkProtocolTest {

    private AnpNetworkProtocol protocol;
    private AnpClient anpClient;

    @BeforeEach
    void setUp() {
        InMemoryAnpKeyStore keyStore = new InMemoryAnpKeyStore();
        AnpAuthenticator auth = new AnpAuthenticator();
        AnpMetaProtocolNegotiator meta = new AnpMetaProtocolNegotiator();

        byte[] secret = "test-secret-key-32bytes-long!!".getBytes(StandardCharsets.UTF_8);
        AnpIdentityKeyPair key = new AnpIdentityKeyPair("key-1", "hmac-sha256", secret, secret);
        keyStore.storeKey("sender-agent", key);

        anpClient = new AnpClient(keyStore, auth, meta);
        AnpNetworkClient client = new AnpNetworkClient(anpClient);
        AnpProtocolConfig config = AnpProtocolConfig.enabled("test.wayang.local");

        protocol = new AnpNetworkProtocol(client, config);
    }

    @Test
    void protocolMetadataIsCorrect() {
        assertEquals("anp", protocol.id());
        assertEquals("1.1", protocol.version());
        assertTrue(protocol.isEnabled());
        assertNotNull(protocol.client());
    }

    @Test
    void invokesRemoteAnpAgentSuccessfully() throws Exception {
        AgentRef target = AgentRef.remote(
                "did:wba:remote.agent.org:analyst",
                "Remote Analyst",
                URI.create("anp://did:wba:remote.agent.org:analyst")
        );

        AgentRequest agentRequest = AgentRequest.of(
                target,
                CapabilityId.of("analysis.report"),
                MessagePayload.text("Please analyze the repo")
        );

        SecurityContextSnapshot security = new SecurityContextSnapshot(
                Principal.agent("sender-agent", "Sender Agent"),
                TenantContext.empty(),
                Optional.empty(),
                Map.of()
        );

        AgentNetworkRequest netRequest = AgentNetworkRequest.of(agentRequest, security);

        AgentNetworkResponse response = protocol.client().invoke(netRequest).toCompletableFuture().get();

        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("anp/1.1", response.metadata().get("protocol"));
        assertEquals("did:wba:remote.agent.org:analyst", response.metadata().get("targetDid"));
    }
}
