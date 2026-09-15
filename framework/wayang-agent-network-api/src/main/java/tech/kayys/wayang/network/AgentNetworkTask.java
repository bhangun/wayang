package tech.kayys.wayang.network;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * Handle representing an asynchronous task in flight across the agent network.
 */
public interface AgentNetworkTask {

    String networkTaskId();

    String executionId();

    CompletionStage<AgentNetworkResponse> result();

    Flow.Publisher<AgentNetworkEvent> events();

    CompletionStage<Void> cancel();
}
