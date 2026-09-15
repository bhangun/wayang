package tech.kayys.wayang.a2a.execution;

import tech.kayys.wayang.communication.api.AgentCommunicator;
import tech.kayys.wayang.communication.api.AgentResponse;
import tech.kayys.wayang.communication.api.AgentTask;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.communication.task.TaskStatus;
import tech.kayys.wayang.security.context.SecurityContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public final class DefaultA2AExecutionBridge implements A2AExecutionBridge {

    private final AgentCommunicator communicator;
    private final A2ATaskRegistry taskRegistry;

    public DefaultA2AExecutionBridge(
            AgentCommunicator communicator,
            A2ATaskRegistry taskRegistry
    ) {
        this.communicator = Objects.requireNonNull(communicator, "communicator");
        this.taskRegistry = Objects.requireNonNull(taskRegistry, "taskRegistry");
    }

    @Override
    public CompletionStage<AgentResponse> execute(A2AExecutionRequest request) {
        ProtocolContext protoCtx = createProtocolContext(request);
        return communicator.send(request.request(), protoCtx);
    }

    @Override
    public CompletionStage<A2ATaskExecution> submit(A2AExecutionRequest request) {
        ProtocolContext protoCtx = createProtocolContext(request);
        String executionId = "exec-" + UUID.randomUUID();

        // Submit to communicator
        AgentTask agentTask = communicator.submit(request.request(), protoCtx);

        CompletionStage<AgentResponse> resultStage = agentTask != null
                ? agentTask.result().thenApply(res -> new AgentResponse(res.success(), res.payload(), res.metadata(), res.error()))
                : CompletableFuture.completedFuture(null);

        A2ATaskExecution execution = new SimpleA2ATaskExecution(
                request.taskId(),
                executionId,
                agentTask != null ? agentTask.status() : TaskStatus.SUBMITTED,
                resultStage
        );

        taskRegistry.register(execution);
        return CompletableFuture.completedFuture(execution);
    }

    private ProtocolContext createProtocolContext(A2AExecutionRequest request) {
        Map<String, Object> attrs = new HashMap<>(request.protocolMetadata());
        if (request.security() != null) {
            SecurityContext secCtx = SecurityContext.of(
                    request.security().principal(),
                    request.security().tenant()
            );
            attrs.put("wayang.security.context", secCtx);
        }
        return new ProtocolContext(
                request.security() != null ? request.security().tenant().tenantId() : null,
                attrs
        );
    }

    private record SimpleA2ATaskExecution(
            String a2aTaskId,
            String executionId,
            TaskStatus status,
            CompletionStage<AgentResponse> result
    ) implements A2ATaskExecution {
        @Override
        public CompletionStage<Void> cancel() {
            return CompletableFuture.completedFuture(null);
        }
    }
}
