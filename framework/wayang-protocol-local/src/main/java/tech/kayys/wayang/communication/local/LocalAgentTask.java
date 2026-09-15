package tech.kayys.wayang.communication.local;

import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.protocol.ProtocolContext;
import tech.kayys.wayang.communication.task.TaskId;
import tech.kayys.wayang.communication.task.TaskStatus;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public final class LocalAgentTask implements AgentTask {

    private final TaskId id;
    private final AgentRequest request;
    private final ProtocolContext context;
    private final LocalProtocolClient client;

    private final CompletableFuture<AgentResult> result;
    private final SubmissionPublisher<AgentEvent> events;

    private volatile TaskStatus status;

    public LocalAgentTask(
            AgentRequest request,
            ProtocolContext context,
            LocalProtocolClient client
    ) {
        this.id = TaskId.random();
        this.request = Objects.requireNonNull(request, "request");
        this.context = Objects.requireNonNull(context, "context");
        this.client = Objects.requireNonNull(client, "client");

        this.result = new CompletableFuture<>();
        this.events = new SubmissionPublisher<>();

        this.status = TaskStatus.SUBMITTED;

        execute();
    }

    private void execute() {
        events.submit(AgentEvent.submitted(id));
        status = TaskStatus.WORKING;
        events.submit(AgentEvent.started(id));

        client.send(request, context)
                .whenComplete((response, error) -> {
                    if (error != null) {
                        status = TaskStatus.FAILED;
                        var agentResult = AgentResult.failure(error);
                        result.complete(agentResult);
                        events.submit(AgentEvent.failed(id, error));
                    } else if (!response.success()) {
                        status = TaskStatus.FAILED;
                        var agentResult = AgentResult.failure(response.error());
                        result.complete(agentResult);
                        events.submit(AgentEvent.failed(id, response.error()));
                    } else {
                        status = TaskStatus.COMPLETED;
                        var agentResult = AgentResult.from(response);
                        result.complete(agentResult);
                        events.submit(AgentEvent.completed(id, response));
                    }
                    events.close();
                });
    }

    @Override
    public TaskId id() {
        return id;
    }

    @Override
    public TaskStatus status() {
        return status;
    }

    @Override
    public AgentRef agent() {
        return request.target();
    }

    @Override
    public CompletionStage<AgentResult> result() {
        return result;
    }

    @Override
    public Flow.Publisher<AgentEvent> events() {
        return events;
    }

    @Override
    public CompletionStage<Void> cancel() {
        this.status = TaskStatus.CANCELED;
        result.complete(AgentResult.failure(AgentError.of("CANCELLED", "Local task was cancelled")));
        events.submit(AgentEvent.canceled(id));
        events.close();
        return CompletableFuture.completedFuture(null);
    }
}
