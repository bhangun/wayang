package tech.kayys.wayang.network.endpoint;

import tech.kayys.wayang.communication.api.AgentRef;

import java.util.concurrent.CompletionStage;

public interface AgentEndpointResolver {

    CompletionStage<ResolvedEndpoint> resolve(AgentRef agent);
}
