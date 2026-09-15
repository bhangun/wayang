package tech.kayys.wayang.network.core;

import org.junit.jupiter.api.Test;
import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.capability.CapabilityId;
import tech.kayys.wayang.communication.message.MessagePayload;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.network.AgentNetworkRequest;
import tech.kayys.wayang.network.AgentNetworkResponse;
import tech.kayys.wayang.network.capability.CapabilityMatch;
import tech.kayys.wayang.network.capability.CapabilityNegotiator;
import tech.kayys.wayang.network.core.local.LocalAgentNetworkProtocol;
import tech.kayys.wayang.network.endpoint.AgentEndpointResolver;
import tech.kayys.wayang.network.endpoint.ResolvedEndpoint;
import tech.kayys.wayang.network.trust.AgentTrustService;
import tech.kayys.wayang.network.trust.TrustDecision;
import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

import static org.junit.jupiter.api.Assertions.*;

class AgentNetworkEndToEndTest {

    @Test
    void testLocalNetworkInvocationSucceeds() {
        // Mock communicator
        AgentCommunicator communicator = new AgentCommunicator() {
            @Override
            public java.util.concurrent.CompletionStage<AgentResponse> send(AgentRequest request, ProtocolContext context) {
                return CompletableFuture.completedFuture(
                        AgentResponse.success(MessagePayload.text("processed: " + request.payload().value()))
                );
            }

            @Override public AgentTask submit(AgentRequest request, ProtocolContext context) { return null; }
            @Override public Flow.Publisher<AgentEvent> stream(AgentRequest request, ProtocolContext context) { return null; }
        };

        // Register LOCAL protocol
        DefaultAgentNetworkProtocolRegistry registry = new DefaultAgentNetworkProtocolRegistry();
        registry.register(new LocalAgentNetworkProtocol(communicator));

        // Resolver returning LOCAL endpoint
        AgentEndpointResolver resolver = agent -> CompletableFuture.completedFuture(
                ResolvedEndpoint.of(URI.create("wayang://local/worker"), "LOCAL", "1.0")
        );

        AgentTrustService trustService = req -> CompletableFuture.completedFuture(TrustDecision.accept());

        CapabilityNegotiator negotiator = (req, caps) -> CompletableFuture.completedFuture(
                CapabilityMatch.match(List.of("compute"))
        );

        DefaultAgentNetwork network = new DefaultAgentNetwork(resolver, trustService, negotiator, registry);

        AgentRef target = AgentRef.local("worker", "Worker", "runtime-1");
        AgentRequest request = AgentRequest.of(target, CapabilityId.of("compute"), MessagePayload.text("10*10"));
        SecurityContext secCtx = SecurityContext.of(Principal.agent("caller", "Caller"), TenantContext.of("tenant-1"));
        SecurityContextSnapshot secSnap = SecurityContextSnapshot.from(secCtx, Optional.empty());

        AgentNetworkRequest netReq = AgentNetworkRequest.of(request, secSnap);

        AgentNetworkResponse response = network.invoke(netReq).toCompletableFuture().join();
        assertNotNull(response);
        assertTrue(response.success());
        assertEquals("processed: 10*10", response.response().payload().value());
    }
}
