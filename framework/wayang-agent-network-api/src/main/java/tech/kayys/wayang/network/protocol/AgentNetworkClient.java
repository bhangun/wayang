package tech.kayys.wayang.network.protocol;

import tech.kayys.wayang.network.AgentNetworkRequest;
import tech.kayys.wayang.network.AgentNetworkResponse;
import tech.kayys.wayang.network.AgentNetworkTask;

import java.util.concurrent.CompletionStage;

public interface AgentNetworkClient {

    CompletionStage<AgentNetworkResponse> invoke(AgentNetworkRequest request);

    CompletionStage<AgentNetworkTask> submit(AgentNetworkRequest request);
}
