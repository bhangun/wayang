package tech.kayys.wayang.network.core.local;

import tech.kayys.wayang.communication.api.AgentCommunicator;
import tech.kayys.wayang.communication.api.AgentResponse;
import tech.kayys.wayang.communication.api.AgentTask;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.network.AgentNetworkEvent;
import tech.kayys.wayang.network.AgentNetworkRequest;
import tech.kayys.wayang.network.AgentNetworkResponse;
import tech.kayys.wayang.network.AgentNetworkTask;
import tech.kayys.wayang.network.protocol.AgentNetworkClient;
import tech.kayys.wayang.security.context.SecurityContext;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

public final class LocalAgentNetworkClient implements AgentNetworkClient {

    private final AgentCommunicator communicator;

    public LocalAgentNetworkClient(AgentCommunicator communicator) {
        this.communicator = Objects.requireNonNull(communicator, "communicator");
    }

    @Override
    public CompletionStage<AgentNetworkResponse> invoke(AgentNetworkRequest request) {
        ProtocolContext ctx = createProtocolContext(request);
        return communicator.send(request.request(), ctx)
                .thenApply(AgentNetworkResponse::of);
    }

    @Override
    public CompletionStage<AgentNetworkTask> submit(AgentNetworkRequest request) {
        ProtocolContext ctx = createProtocolContext(request);
        String networkTaskId = "net-task-" + UUID.randomUUID();
        String executionId   = "exec-" + UUID.randomUUID();

        AgentTask agentTask = communicator.submit(request.request(), ctx);

        AgentNetworkTask task = new AgentNetworkTask() {
            @Override public String networkTaskId() { return networkTaskId; }
            @Override public String executionId()   { return executionId; }

            @Override
            public CompletionStage<AgentNetworkResponse> result() {
                return agentTask.result().thenApply(res ->
                        AgentNetworkResponse.of(new AgentResponse(res.success(), res.payload(), res.metadata(), res.error()))
                );
            }

            @Override
            public Flow.Publisher<AgentNetworkEvent> events() {
                return null;
            }

            @Override
            public CompletionStage<Void> cancel() {
                return agentTask.cancel();
            }
        };

        return CompletableFuture.completedFuture(task);
    }

    private ProtocolContext createProtocolContext(AgentNetworkRequest request) {
        SecurityContext secCtx = request.securityContext() != null
                ? SecurityContext.of(request.securityContext().principal(), request.securityContext().tenant())
                : SecurityContext.anonymous();

        return new ProtocolContext(
                secCtx.tenant().tenantId(),
                Map.of("wayang.security.context", secCtx)
        );
    }
}
