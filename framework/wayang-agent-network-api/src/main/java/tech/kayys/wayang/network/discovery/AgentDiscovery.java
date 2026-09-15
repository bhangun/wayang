package tech.kayys.wayang.network.discovery;

import tech.kayys.wayang.communication.api.AgentDescriptor;

import java.util.concurrent.CompletionStage;

public interface AgentDiscovery {

    CompletionStage<DiscoveryResult> discover(DiscoveryQuery query);

    CompletionStage<AgentDescriptor> resolve(String agentId);
}
