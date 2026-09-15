package tech.kayys.wayang.anp.client;

import tech.kayys.wayang.anp.identity.DidWbaIdentity;
import tech.kayys.wayang.communication.api.AgentResponse;
import tech.kayys.wayang.network.protocol.AgentNetworkClient;
import tech.kayys.wayang.network.AgentNetworkRequest;
import tech.kayys.wayang.network.AgentNetworkResponse;
import tech.kayys.wayang.network.AgentNetworkTask;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Implements {@link AgentNetworkClient} for the ANP protocol.
 */
public final class AnpNetworkClient implements AgentNetworkClient {

    private final AnpClient anpClient;

    public AnpNetworkClient(AnpClient anpClient) {
        this.anpClient = Objects.requireNonNull(anpClient, "anpClient");
    }

    @Override
    public CompletionStage<AgentNetworkResponse> invoke(AgentNetworkRequest request) {
        DidWbaIdentity did = resolveTargetDid(request);
        String senderId = request.securityContext().principal().id();

        return anpClient.send(did, request.request().payload(), senderId)
                .thenApply(agentResponse -> new AgentNetworkResponse(
                        agentResponse,
                        Map.of("protocol", "anp/1.1", "targetDid", did.raw())
                ));
    }

    @Override
    public CompletionStage<AgentNetworkTask> submit(AgentNetworkRequest request) {
        String networkTaskId = "anp-task-" + UUID.randomUUID();
        String executionId = "anp-exec-" + UUID.randomUUID();

        CompletableFuture<AgentNetworkResponse> future = invoke(request).toCompletableFuture();
        AgentNetworkTask task = new AnpNetworkTaskImpl(networkTaskId, executionId, future);
        return CompletableFuture.completedFuture(task);
    }

    private DidWbaIdentity resolveTargetDid(AgentNetworkRequest request) {
        String targetId = request.request().target().id();
        if (targetId.startsWith(DidWbaIdentity.PREFIX)) {
            return DidWbaIdentity.parse(targetId);
        }
        // Fallback: wrap target name in local/default domain DID
        return DidWbaIdentity.of("network.wayang.local", targetId);
    }
}
