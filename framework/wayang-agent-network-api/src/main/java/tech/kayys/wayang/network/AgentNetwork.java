package tech.kayys.wayang.network;

import java.util.concurrent.CompletionStage;

/**
 * Primary contract for the distributed Agent Network Layer (ANP).
 */
public interface AgentNetwork {

    CompletionStage<AgentNetworkResponse> invoke(AgentNetworkRequest request);

    CompletionStage<AgentNetworkTask> submit(AgentNetworkRequest request);
}
