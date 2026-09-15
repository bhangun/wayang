package tech.kayys.wayang.anp.client;

import tech.kayys.wayang.network.AgentNetworkEvent;
import tech.kayys.wayang.network.AgentNetworkResponse;
import tech.kayys.wayang.network.AgentNetworkTask;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * Handle representing an asynchronous ANP task in flight.
 */
public final class AnpNetworkTaskImpl implements AgentNetworkTask {

    private final String networkTaskId;
    private final String executionId;
    private final CompletableFuture<AgentNetworkResponse> resultFuture;

    public AnpNetworkTaskImpl(
            String networkTaskId,
            String executionId,
            CompletableFuture<AgentNetworkResponse> resultFuture) {
        this.networkTaskId = Objects.requireNonNull(networkTaskId, "networkTaskId");
        this.executionId = Objects.requireNonNull(executionId, "executionId");
        this.resultFuture = Objects.requireNonNull(resultFuture, "resultFuture");
    }

    @Override
    public String networkTaskId() {
        return networkTaskId;
    }

    @Override
    public String executionId() {
        return executionId;
    }

    @Override
    public CompletionStage<AgentNetworkResponse> result() {
        return resultFuture;
    }

    @Override
    public Flow.Publisher<AgentNetworkEvent> events() {
        return subscriber -> {
            // Minimal no-op publisher for base implementation
        };
    }

    @Override
    public CompletionStage<Void> cancel() {
        resultFuture.cancel(true);
        return CompletableFuture.completedFuture(null);
    }
}
