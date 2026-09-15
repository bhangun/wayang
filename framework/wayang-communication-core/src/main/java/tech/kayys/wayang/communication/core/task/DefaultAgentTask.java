package tech.kayys.wayang.communication.core.task;

import tech.kayys.wayang.communication.api.*;
import tech.kayys.wayang.communication.task.TaskId;
import tech.kayys.wayang.communication.task.TaskStatus;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

public class DefaultAgentTask implements AgentTask, TaskController {

    private final TaskId id;
    private final AgentRef agent;
    private volatile TaskStatus status;
    private final CompletableFuture<AgentResult> resultFuture;
    private final SubmissionPublisher<AgentEvent> eventPublisher;
    private volatile Runnable cancelHandler;

    public DefaultAgentTask(TaskId id, AgentRef agent) {
        this.id = Objects.requireNonNull(id, "id");
        this.agent = Objects.requireNonNull(agent, "agent");
        this.status = TaskStatus.SUBMITTED;
        this.resultFuture = new CompletableFuture<>();
        this.eventPublisher = new SubmissionPublisher<>();
    }

    public static DefaultAgentTask create(AgentRef agent) {
        return new DefaultAgentTask(TaskId.random(), agent);
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
        return agent;
    }

    @Override
    public CompletionStage<AgentResult> result() {
        return resultFuture;
    }

    @Override
    public Flow.Publisher<AgentEvent> events() {
        return eventPublisher;
    }

    @Override
    public CompletionStage<Void> cancel() {
        this.status = TaskStatus.CANCELED;
        if (cancelHandler != null) {
            try {
                cancelHandler.run();
            } catch (Exception ignored) {
            }
        }
        resultFuture.complete(AgentResult.failure(AgentError.of("CANCELLED", "Task was cancelled")));
        eventPublisher.close();
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void updateStatus(TaskStatus newStatus) {
        this.status = newStatus;
    }

    @Override
    public void publishEvent(AgentEvent event) {
        if (!eventPublisher.isClosed()) {
            eventPublisher.submit(event);
        }
    }

    @Override
    public void complete(AgentResult result) {
        this.status = TaskStatus.COMPLETED;
        resultFuture.complete(result);
        eventPublisher.close();
    }

    @Override
    public void fail(AgentError error) {
        this.status = TaskStatus.FAILED;
        resultFuture.complete(AgentResult.failure(error));
        eventPublisher.close();
    }

    @Override
    public void onCancel(Runnable cancelHandler) {
        this.cancelHandler = cancelHandler;
    }
}
