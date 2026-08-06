package tech.kayys.wayang.execution.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import tech.kayys.wayang.execution.CheckpointStore;
import tech.kayys.wayang.execution.EventBus;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionExecutor;
import tech.kayys.wayang.execution.ExecutionGraph;
import tech.kayys.wayang.execution.ExecutionNode;
import tech.kayys.wayang.execution.ExecutionScheduler;
import tech.kayys.wayang.execution.ExecutionSnapshot;
import tech.kayys.wayang.execution.ExecutionState;
import tech.kayys.wayang.execution.NodeResult;
import tech.kayys.wayang.execution.NodeStatus;
import tech.kayys.wayang.harness.spi.ExecutionOptions;

/**
 * Default implementation of the ExecutionExecutor.
 * 
 * <p>
 * Executes an ExecutionGraph using the scheduler to determine
 * which nodes to run and when.
 * 
 * <p>
 * Key features:
 * <ul>
 * <li>Thread pool execution</li>
 * <li>Parallel node execution</li>
 * <li>Checkpoint support</li>
 * <li>Event emission</li>
 * <li>Resource management</li>
 * <li>Timeout handling</li>
 * <li>Graceful shutdown</li>
 * </ul>
 */
public class DefaultExecutionExecutor implements ExecutionExecutor {

    private final ExecutionScheduler scheduler;
    private final ExecutorService executorService;
    private final EventBus eventBus;
    private final CheckpointStore checkpointStore;
    private final AtomicBoolean running;
    private final Map<UUID, Future<?>> runningFutures;

    public DefaultExecutionExecutor(
            ExecutionScheduler scheduler,
            EventBus eventBus,
            CheckpointStore checkpointStore) {

        this.scheduler = scheduler;
        this.eventBus = eventBus;
        this.checkpointStore = checkpointStore;
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors() * 2);
        this.running = new AtomicBoolean(false);
        this.runningFutures = new ConcurrentHashMap<>();
    }

    @Override
    public ExecutionResult execute(ExecutionGraph graph) {
        return execute(graph, ExecutionOptions.defaults());
    }

    @Override
    public ExecutionResult execute(ExecutionGraph graph, ExecutionOptions options) {
        // Validate graph
        if (!graph.validate()) {
            throw new IllegalStateException("Invalid execution graph");
        }

        // Create execution context
        ExecutionContext context = createContext(graph, options);

        // Initialize execution state
        ExecutionState state = new DefaultExecutionState(context);

        // Emit start event
        eventBus.publish(new ExecutionStartedEvent(context));

        running.set(true);
        Instant startTime = Instant.now();

        try {
            // Check for checkpoint
            if (options.resumeFromCheckpoint()) {
                ExecutionSnapshot snapshot = checkpointStore.restore(context.executionId());
                if (snapshot != null) {
                    state = snapshot.state();
                    log.info("Resumed execution from checkpoint");
                }
            }

            // Main execution loop
            while (running.get() && !isComplete(graph, state)) {
                // Check for timeout
                if (options.timeout().isPresent() &&
                        Duration.between(startTime, Instant.now()).compareTo(options.timeout().get()) > 0) {
                    throw new TimeoutException("Execution timed out");
                }

                // Get ready nodes from scheduler
                List<ExecutionNode> readyNodes = scheduler.schedule(graph, state);

                if (readyNodes.isEmpty()) {
                    // Deadlock or completion
                    if (!isComplete(graph, state)) {
                        log.warn("No nodes ready but execution not complete - possible deadlock");
                        // Check for wait conditions
                        boolean hasWaitingNodes = graph.nodes().stream()
                                .anyMatch(node -> node.status() == NodeStatus.WAITING);
                        if (!hasWaitingNodes) {
                            break;
                        }
                    }
                }

                // Submit ready nodes for execution
                List<CompletableFuture<NodeResult>> futures = new ArrayList<>();
                for (ExecutionNode node : readyNodes) {
                    CompletableFuture<NodeResult> future = submitNode(node, context, state);
                    futures.add(future);
                }

                // Wait for all nodes to complete
                try {
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .get(options.timeout().orElse(Duration.ofMinutes(30)).toMillis(),
                                    TimeUnit.MILLISECONDS);
                } catch (TimeoutException e) {
                    log.error("Node execution timed out");
                    // Cancel running futures
                    for (CompletableFuture<NodeResult> future : futures) {
                        future.cancel(true);
                    }
                    throw e;
                }

                // Process results
                for (CompletableFuture<NodeResult> future : futures) {
                    NodeResult result = future.get();
                    scheduler.updateState(graph, state, result);
                    eventBus.publish(new NodeCompletedEvent(result));
                }

                // Checkpoint if configured
                if (options.checkpointInterval().isPresent()) {
                    int completedNodes = state.getCompletedNodes().size();
                    if (completedNodes % options.checkpointInterval().get() == 0) {
                        checkpointStore.save(new ExecutionSnapshot(state));
                    }
                }
            }

            // Build result
            return ExecutionResult.builder()
                    .executionId(context.executionId())
                    .graphId(graph.id())
                    .startTime(startTime)
                    .endTime(Instant.now())
                    .status(getFinalStatus(state))
                    .metrics(state.getMetrics())
                    .result(state.getFinalResult())
                    .build();

        } catch (Exception e) {
            log.error("Execution failed", e);
            eventBus.publish(new ExecutionFailedEvent(context, e));
            return ExecutionResult.failed(context.executionId(), e.getMessage());
        } finally {
            running.set(false);
            eventBus.publish(new ExecutionCompletedEvent(context));

            // Cleanup resources
            if (context.resources() != null) {
                context.resources().cleanup();
            }
        }
    }

    @Override
    public void cancel(UUID executionId) {
        running.set(false);
        // Cancel all running futures
        for (Future<?> future : runningFutures.values()) {
            future.cancel(true);
        }
        runningFutures.clear();

        // Emit cancellation event
        eventBus.publish(new ExecutionCancelledEvent(executionId));
    }

    @Override
    public ExecutionStatus getStatus(UUID executionId) {
        return null; // Implementation would query state
    }

    private CompletableFuture<NodeResult> submitNode(
            ExecutionNode node,
            ExecutionContext context,
            ExecutionState state) {

        // Acquire resources
        if (!node.resourceRequirements().isEmpty()) {
            ResourceLease lease = context.resources().acquire(node.resourceRequirements());
            if (lease == null) {
                state.updateNodeStatus(node.id(), NodeStatus.WAITING);
                return CompletableFuture.completedFuture(
                        NodeResult.waiting(node.id(), "Resources not available"));
            }
        }

        // Submit to executor
        CompletableFuture<NodeResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                eventBus.publish(new NodeStartedEvent(node));
                return node.execute(context).get();
            } catch (Exception e) {
                log.error("Node execution failed: {}", node.id(), e);
                return NodeResult.failure(node.id(), e.getMessage());
            }
        }, executorService);

        // Track future
        runningFutures.put(node.id(), future);

        // Remove when complete
        future.whenComplete((result, error) -> {
            runningFutures.remove(node.id());
            state.updateNodeResult(node.id(), result);
        });

        return future;
    }

    private ExecutionContext createContext(ExecutionGraph graph, ExecutionOptions options) {
        // Implementation creates proper context
        return null;
    }

    private boolean isComplete(ExecutionGraph graph, ExecutionState state) {
        // Check if all nodes are in terminal states
        return graph.nodes().stream()
                .allMatch(node -> {
                    NodeStatus status = state.getNodeStatus(node.id());
                    return status == NodeStatus.COMPLETED ||
                            status == NodeStatus.FAILED ||
                            status == NodeStatus.SKIPPED ||
                            status == NodeStatus.CANCELLED;
                });
    }

    private ExecutionStatus getFinalStatus(ExecutionState state) {
        // Determine overall execution status
        for (ExecutionNode node : state.getNodes()) {
            if (node.status() == NodeStatus.FAILED) {
                return ExecutionStatus.FAILED;
            }
        }
        return ExecutionStatus.COMPLETED;
    }
}