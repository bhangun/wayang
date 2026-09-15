package tech.kayys.wayang.communication.local;

import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.endpoint.LocalAgentEndpoint;
import tech.kayys.wayang.communication.exception.AgentUnavailableException;
import tech.kayys.wayang.communication.protocol.ProtocolClient;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.communication.task.TaskId;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public final class LocalProtocolClient implements ProtocolClient {

    private final LocalAgentRegistry registry;

    public LocalProtocolClient(LocalAgentRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public CompletionStage<AgentResponse> send(
            AgentRequest request,
            ProtocolContext context
    ) {
        Objects.requireNonNull(request, "request");

        var target = request.target();

        if (!(target.endpoint() instanceof LocalAgentEndpoint)) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException(
                            "Target is not a local endpoint: " + target.id()
                    )
            );
        }

        var handlerOpt = registry.find(target.id());

        if (handlerOpt.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new AgentUnavailableException(
                            "Local agent is not registered: " + target.id()
                    )
            );
        }

        try {
            return handlerOpt.get().handle(request);
        } catch (Throwable error) {
            return CompletableFuture.failedFuture(error);
        }
    }

    @Override
    public AgentTask submit(
            AgentRequest request,
            ProtocolContext context
    ) {
        return new LocalAgentTask(
                request,
                context != null ? context : ProtocolContext.empty(),
                this
        );
    }

    @Override
    public Flow.Publisher<AgentEvent> stream(
            AgentRequest request,
            ProtocolContext context
    ) {
        var publisher = new SubmissionPublisher<AgentEvent>();
        var taskId = TaskId.random();

        publisher.submit(AgentEvent.started(taskId));

        send(request, context)
                .whenComplete((response, error) -> {
                    if (error != null) {
                        publisher.submit(AgentEvent.failed(taskId, error));
                    } else {
                        publisher.submit(AgentEvent.completed(taskId, response));
                    }
                    publisher.close();
                });

        return publisher;
    }
}
