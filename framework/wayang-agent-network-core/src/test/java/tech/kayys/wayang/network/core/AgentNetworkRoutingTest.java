package tech.kayys.wayang.network.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.kayys.wayang.communication.api.AgentRef;
import tech.kayys.wayang.communication.api.AgentRequest;
import tech.kayys.wayang.communication.api.AgentResponse;
import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.network.AgentNetworkRequest;
import tech.kayys.wayang.network.AgentNetworkResponse;
import tech.kayys.wayang.network.AgentNetworkTask;
import tech.kayys.wayang.network.capability.CapabilityMatch;
import tech.kayys.wayang.network.capability.CapabilityNegotiator;
import tech.kayys.wayang.network.endpoint.AgentEndpointResolver;
import tech.kayys.wayang.network.endpoint.ResolvedEndpoint;
import tech.kayys.wayang.network.exception.AgentNetworkException;
import tech.kayys.wayang.network.exception.TrustException;
import tech.kayys.wayang.network.protocol.AgentNetworkClient;
import tech.kayys.wayang.network.protocol.AgentNetworkProtocol;
import tech.kayys.wayang.network.trust.AgentTrustService;
import tech.kayys.wayang.network.trust.TrustDecision;
import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class AgentNetworkRoutingTest {

    private DefaultAgentNetworkProtocolRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new DefaultAgentNetworkProtocolRegistry();
    }

    @Test
    void testRejectionWhenUntrusted() {
        AgentEndpointResolver resolver = agent -> CompletableFuture.completedFuture(
                ResolvedEndpoint.of(URI.create("https://remote/agent"), "ANP", "1.0")
        );

        AgentTrustService trustService = req -> CompletableFuture.completedFuture(
                TrustDecision.reject("Untrusted domain")
        );

        CapabilityNegotiator negotiator = (req, caps) -> CompletableFuture.completedFuture(
                CapabilityMatch.match(List.of("search"))
        );

        DefaultAgentNetwork network = new DefaultAgentNetwork(resolver, trustService, negotiator, registry);

        AgentRef target = AgentRef.remote("untrusted-agent", "Untrusted", URI.create("https://remote/agent"));
        AgentRequest agentReq = AgentRequest.of(target, CapabilityId.of("search"), MessagePayload.text("query"));
        SecurityContext secCtx = SecurityContext.of(Principal.anonymous(), TenantContext.empty());
        SecurityContextSnapshot secSnap = SecurityContextSnapshot.from(secCtx, Optional.empty());

        AgentNetworkRequest netReq = AgentNetworkRequest.of(agentReq, secSnap);

        Exception ex = assertThrows(Exception.class, () -> network.invoke(netReq).toCompletableFuture().join());
        assertTrue(ex.getCause() instanceof TrustException || ex instanceof TrustException);
    }

    @Test
    void testRejectionWhenCapabilityMismatch() {
        AgentEndpointResolver resolver = agent -> CompletableFuture.completedFuture(
                ResolvedEndpoint.of(URI.create("https://remote/agent"), "ANP", "1.0")
        );

        AgentTrustService trustService = req -> CompletableFuture.completedFuture(TrustDecision.accept());

        CapabilityNegotiator negotiator = (req, caps) -> CompletableFuture.completedFuture(
                CapabilityMatch.noMatch(List.of("admin.wipe"))
        );

        DefaultAgentNetwork network = new DefaultAgentNetwork(resolver, trustService, negotiator, registry);

        AgentRef target = AgentRef.remote("agent", "Agent", URI.create("https://remote/agent"));
        AgentRequest agentReq = AgentRequest.of(target, CapabilityId.of("admin.wipe"), MessagePayload.text("wipe"));
        SecurityContext secCtx = SecurityContext.of(Principal.anonymous(), TenantContext.empty());
        SecurityContextSnapshot secSnap = SecurityContextSnapshot.from(secCtx, Optional.empty());

        AgentNetworkRequest netReq = AgentNetworkRequest.of(agentReq, secSnap);

        Exception ex = assertThrows(Exception.class, () -> network.invoke(netReq).toCompletableFuture().join());
        assertTrue(ex.getCause() instanceof AgentNetworkException || ex instanceof AgentNetworkException);
        assertTrue(ex.getMessage().contains("Capability requirement not satisfied"));
    }
}
