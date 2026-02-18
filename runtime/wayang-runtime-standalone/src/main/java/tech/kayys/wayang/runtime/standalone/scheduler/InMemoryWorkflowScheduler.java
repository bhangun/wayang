package tech.kayys.wayang.runtime.standalone.scheduler;

import io.quarkus.scheduler.Scheduled;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import tech.kayys.gamelan.dispatcher.TaskDispatcherAggregator;
import tech.kayys.gamelan.engine.event.EventPublisher;
import tech.kayys.gamelan.engine.event.ExecutionEvent;
import tech.kayys.gamelan.engine.executor.ExecutorInfo;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.engine.node.NodeId;
import tech.kayys.gamelan.engine.run.RetryPolicy;
import tech.kayys.gamelan.engine.workflow.WorkflowRunId;
import tech.kayys.gamelan.registry.ExecutorRegistry;
import tech.kayys.gamelan.scheduler.WorkflowScheduler;

@ApplicationScoped
public class InMemoryWorkflowScheduler implements WorkflowScheduler {

    @Inject
    TaskDispatcherAggregator taskDispatcher;

    @Inject
    EventPublisher eventPublisher;

    @Inject
    ExecutorRegistry executorRegistry;

    private final Map<String, RetryEntry> retryQueue = new ConcurrentHashMap<>();
    private final Map<String, NodeExecutionTask> activeTasks = new ConcurrentHashMap<>();

    @Override
    public Uni<Void> scheduleTask(NodeExecutionTask task) {
        String taskKey = task.runId().value() + ":" + task.nodeId().value() + ":" + task.attempt();
        activeTasks.put(taskKey, task);

        return executorRegistry.getExecutorForNode(task.nodeId())
                .flatMap((Optional<ExecutorInfo> executorOpt) -> {
                    if (executorOpt.isEmpty()) {
                        return handleDispatchFailure(task, new IllegalStateException("No executor available"));
                    }
                    return taskDispatcher.dispatch(task, executorOpt.get())
                            .onFailure().recoverWithUni(err -> handleDispatchFailure(task, err))
                            .replaceWithVoid();
                });
    }

    @Override
    public Uni<Void> scheduleRetry(WorkflowRunId runId, NodeId nodeId, Duration delay) {
        String key = runId.value() + ":" + nodeId.value();
        retryQueue.put(key, new RetryEntry(runId, nodeId, Instant.now().plus(delay)));
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> cancelTasksForRun(WorkflowRunId runId) {
        String runPrefix = runId.value() + ":";
        activeTasks.keySet().removeIf(k -> k.startsWith(runPrefix));
        retryQueue.keySet().removeIf(k -> k.startsWith(runPrefix));
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> publishEvents(List<ExecutionEvent> events) {
        return events.isEmpty() ? Uni.createFrom().voidItem() : eventPublisher.publish(events);
    }

    @Override
    public Uni<Long> getScheduledTasksCount() {
        return Uni.createFrom().item((long) activeTasks.size() + retryQueue.size());
    }

    @Scheduled(every = "1s")
    void processRetries() {
        Instant now = Instant.now();
        retryQueue.entrySet().removeIf(entry -> {
            RetryEntry retry = entry.getValue();
            if (retry.executeAt().isAfter(now)) {
                return false;
            }
            eventPublisher.publishRetry(retry.runId(), retry.nodeId())
                    .subscribe().with(v -> {
                    }, err -> {
                    });
            return true;
        });
    }

    private Uni<Void> handleDispatchFailure(NodeExecutionTask task, Throwable failure) {
        RetryPolicy retryPolicy = task.retryPolicy();
        int attempt = task.attempt();

        if (retryPolicy == null || !retryPolicy.shouldRetry(attempt)) {
            return publishEvents(List.of(
                    ExecutionEvent.nodeDeadLettered(task.runId(), task.nodeId(), failure.getMessage())));
        }
        Duration delay = retryPolicy.calculateDelay(attempt);
        return scheduleRetry(task.runId(), task.nodeId(), delay);
    }

    private record RetryEntry(WorkflowRunId runId, NodeId nodeId, Instant executeAt) {
    }
}
