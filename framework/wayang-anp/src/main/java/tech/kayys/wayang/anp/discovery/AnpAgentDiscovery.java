package tech.kayys.wayang.anp.discovery;

import tech.kayys.wayang.anp.description.AnpAgentDescription;
import tech.kayys.wayang.anp.description.AnpAgentDescriptionService;
import tech.kayys.wayang.anp.identity.DidWbaIdentity;
import tech.kayys.wayang.communication.api.AgentCapability;
import tech.kayys.wayang.communication.api.AgentDescriptor;
import tech.kayys.wayang.communication.api.AgentEndpointDescriptor;
import tech.kayys.wayang.communication.api.AgentRef;
import tech.kayys.wayang.communication.endpoint.RemoteAgentEndpoint;
import tech.kayys.wayang.network.discovery.AgentDiscovery;
import tech.kayys.wayang.network.discovery.DiscoveryQuery;
import tech.kayys.wayang.network.discovery.DiscoveryResult;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Implements the {@link AgentDiscovery} SPI for ANP agents.
 */
public final class AnpAgentDiscovery implements AgentDiscovery {

    private final AnpAgentDescriptionService localService;
    private final AnpDiscoveryClient remoteClient;

    public AnpAgentDiscovery(
            AnpAgentDescriptionService localService,
            AnpDiscoveryClient remoteClient) {
        this.localService = Objects.requireNonNull(localService, "localService");
        this.remoteClient = Objects.requireNonNull(remoteClient, "remoteClient");
    }

    @Override
    public CompletionStage<DiscoveryResult> discover(DiscoveryQuery query) {
        List<AgentDescriptor> matched = new ArrayList<>();

        for (AnpAgentDescription desc : localService.all()) {
            if (query.capability() == null || query.capability().isBlank() || desc.hasCapability(query.capability())) {
                matched.add(toDescriptor(desc));
            }
        }

        return CompletableFuture.completedFuture(DiscoveryResult.of(matched));
    }

    @Override
    public CompletionStage<AgentDescriptor> resolve(String agentId) {
        // Try local description first
        var local = localService.findByDid(agentId);
        if (local.isPresent()) {
            return CompletableFuture.completedFuture(toDescriptor(local.get()));
        }

        // Try remote discovery
        if (agentId.startsWith(DidWbaIdentity.PREFIX)) {
            DidWbaIdentity did = DidWbaIdentity.parse(agentId);
            return remoteClient.fetch(did).thenApply(opt ->
                    opt.map(this::toDescriptor)
                       .orElseGet(() -> toFallbackDescriptor(did))
            );
        }

        return CompletableFuture.failedFuture(
                new IllegalArgumentException("Unsupported ANP agent identifier: " + agentId)
        );
    }

    private AgentDescriptor toDescriptor(AnpAgentDescription desc) {
        AgentRef ref = AgentRef.remote(
                desc.did(),
                desc.name(),
                URI.create("anp://" + desc.did())
        );

        List<AgentCapability> caps = desc.capabilities().stream()
                .map(c -> AgentCapability.of(c.id(), c.description()))
                .toList();

        List<AgentEndpointDescriptor> endpoints = desc.endpoints().entrySet().stream()
                .map(e -> new AgentEndpointDescriptor(
                        RemoteAgentEndpoint.of(URI.create(e.getValue())),
                        "ANP",
                        "1.1",
                        Map.of("role", e.getKey())
                ))
                .toList();

        return new AgentDescriptor(ref, desc.description(), caps, endpoints, desc.metadata());
    }

    private AgentDescriptor toFallbackDescriptor(DidWbaIdentity did) {
        AgentRef ref = AgentRef.remote(
                did.raw(),
                did.host(),
                URI.create("anp://" + did.raw())
        );
        return AgentDescriptor.of(ref, "ANP Agent at " + did.host());
    }
}
