package tech.kayys.wayang.network.core;

import tech.kayys.wayang.network.*;
import tech.kayys.wayang.network.capability.CapabilityMatch;
import tech.kayys.wayang.network.capability.CapabilityNegotiator;
import tech.kayys.wayang.network.capability.CapabilityRequirement;
import tech.kayys.wayang.network.capability.MatchStatus;
import tech.kayys.wayang.network.discovery.AgentDiscovery;
import tech.kayys.wayang.network.endpoint.AgentEndpointResolver;
import tech.kayys.wayang.network.endpoint.ResolvedEndpoint;
import tech.kayys.wayang.network.exception.AgentNetworkException;
import tech.kayys.wayang.network.exception.TrustException;
import tech.kayys.wayang.network.protocol.AgentNetworkProtocol;
import tech.kayys.wayang.network.trust.AgentTrustService;
import tech.kayys.wayang.network.trust.TrustDecision;
import tech.kayys.wayang.network.trust.TrustRequest;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DefaultAgentNetwork implements AgentNetwork {

    private final AgentEndpointResolver endpointResolver;
    private final AgentTrustService trustService;
    private final CapabilityNegotiator capabilityNegotiator;
    private final AgentNetworkProtocolRegistry protocolRegistry;

    public DefaultAgentNetwork(
            AgentEndpointResolver endpointResolver,
            AgentTrustService trustService,
            CapabilityNegotiator capabilityNegotiator,
            AgentNetworkProtocolRegistry protocolRegistry
    ) {
        this.endpointResolver    = Objects.requireNonNull(endpointResolver, "endpointResolver");
        this.trustService         = Objects.requireNonNull(trustService, "trustService");
        this.capabilityNegotiator = Objects.requireNonNull(capabilityNegotiator, "capabilityNegotiator");
        this.protocolRegistry     = Objects.requireNonNull(protocolRegistry, "protocolRegistry");
    }

    @Override
    public CompletionStage<AgentNetworkResponse> invoke(AgentNetworkRequest request) {
        return routeAndValidate(request).thenCompose(protocol ->
                protocol.client().invoke(request)
        );
    }

    @Override
    public CompletionStage<AgentNetworkTask> submit(AgentNetworkRequest request) {
        return routeAndValidate(request).thenCompose(protocol ->
                protocol.client().submit(request)
        );
    }

    private CompletionStage<AgentNetworkProtocol> routeAndValidate(AgentNetworkRequest request) {
        var targetAgent = request.request().target();

        // 1. Resolve Endpoint
        return endpointResolver.resolve(targetAgent).thenCompose(endpoint -> {
            // 2. Evaluate Trust
            TrustRequest trustReq = new TrustRequest(
                    targetAgent,
                    endpoint.protocol(),
                    targetAgent.id(),
                    request.securityContext().tenant().tenantId()
            );

            return trustService.evaluate(trustReq).thenCompose(trustDecision -> {
                if (!trustDecision.trusted()) {
                    return CompletableFuture.failedFuture(
                            new TrustException("Agent " + targetAgent.id() + " untrusted: " + trustDecision.reason())
                    );
                }

                // 3. Negotiate Capabilities
                String capName = request.request().capability() != null
                        ? request.request().capability().value()
                        : "default";
                CapabilityRequirement req = CapabilityRequirement.of(capName);

                return capabilityNegotiator.negotiate(req, endpoint.metadata()).thenCompose(capMatch -> {
                    if (capMatch.status() == MatchStatus.NO_MATCH) {
                        return CompletableFuture.failedFuture(
                                new AgentNetworkException("Capability requirement not satisfied: " + capName)
                        );
                    }

                    // 4. Select Protocol
                    AgentNetworkProtocol protocol = protocolRegistry.find(endpoint.protocol())
                            .orElseThrow(() -> new AgentNetworkException("No protocol implementation registered for: " + endpoint.protocol()));

                    return CompletableFuture.completedFuture(protocol);
                });
            });
        });
    }
}
