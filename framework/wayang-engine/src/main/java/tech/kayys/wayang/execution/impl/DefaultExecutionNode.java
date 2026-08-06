package tech.kayys.wayang.execution.impl;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import tech.kayys.wayang.execution.EventBus;
import tech.kayys.wayang.execution.ExecutionContext;
import tech.kayys.wayang.execution.ExecutionEdge;
import tech.kayys.wayang.execution.ExecutionNode;
import tech.kayys.wayang.execution.NodeConfig;
import tech.kayys.wayang.execution.NodeResult;
import tech.kayys.wayang.execution.NodeStatus;
import tech.kayys.wayang.execution.NodeType;
import tech.kayys.wayang.execution.RetryPolicy;
import tech.kayys.wayang.sandbox.ResourceRequirements;

/**
 * Default implementation of ExecutionNode.
 * 
 * <p>
 * Supports all built-in node types and can be extended for custom types.
 * Thread-safe and supports concurrent execution.
 */
public final public class DefaultExecutionNode implements ExecutionNode {

    private final UUID id;
    private final String type;
    private final Optional<String> name;
    private volatile NodeStatus status;
    private final NodeConfig config;
    private final Duration timeout;
    private final RetryPolicy retryPolicy;
    private final boolean parallelizable;
    private final ResourceRequirements resourceRequirements;
    private final Map<UUID, ExecutionEdge> incomingEdges;
    private final Map<UUID, ExecutionEdge> outgoingEdges;
    private final NodeExecutor executor;
    private final EventBus eventBus;

    public DefaultExecutionNode(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.type = builder.type != null ? builder.type : NodeType.TOOL.getType();
        this.name = Optional.ofNullable(builder.name);
        this.status = NodeStatus.CREATED;
        this.config = builder.config != null ? builder.config : NodeConfig.empty();
        this.timeout = builder.timeout != null ? builder.timeout : Duration.ofSeconds(30);
        this.retryPolicy = builder.retryPolicy != null ? builder.retryPolicy : RetryPolicy.noRetry();
        this.parallelizable = builder.parallelizable;
        this.resourceRequirements = builder.resourceRequirements != null ? builder.resourceRequirements
                : ResourceRequirements.empty();
        this.incomingEdges = new ConcurrentHashMap<>();
        this.outgoingEdges = new ConcurrentHashMap<>();
        this.executor = builder.executor != null ? builder.executor
                : (node, context) -> CompletableFuture.completedFuture(NodeResult.success(node.id()));
        this.eventBus = builder.eventBus;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public Optional<String> name() {
        return name;
    }

    @Override
    public NodeStatus status() {
        return status;
    }

    @Override
    public NodeConfig config() {
        return config;
    }

    @Override
    public Duration timeout() {
        return timeout;
    }

    @Override
    public RetryPolicy retryPolicy() {
        return retryPolicy;
    }

    @Override
    public boolean isParallelizable() {
        return parallelizable;
    }

    @Override
    public ResourceRequirements resourceRequirements() {
        return resourceRequirements;
    }

    @Override
    public Collection<ExecutionEdge> incomingEdges() {
        return incomingEdges.values();
    }

    @Override
    public Collection<ExecutionEdge> outgoingEdges() {
        return outgoingEdges.values();
    }

    @Override
    public CompletableFuture<NodeResult> execute(ExecutionContext context) {
        if (status == NodeStatus.CANCELLED || status == NodeStatus.FAILED) {
            return CompletableFuture.completedFuture(
                    NodeResult.failure(id, "Node is " + status.name().toLowerCase()));
        }

        // Update status
        setStatus(NodeStatus.RUNNING);

        // Execute with retry policy
        return executeWithRetry(context, 0);
    }

    private CompletableFuture<NodeResult> executeWithRetry(ExecutionContext context, int attempt) {
        return executor.execute(this, context)
                .thenCompose(result -> {
                    if (result.isSuccess()) {
                        setStatus(NodeStatus.COMPLETED);
                        return CompletableFuture.completedFuture(result);
                    } else if (retryPolicy.shouldRetry(result, attempt)) {
                        setStatus(NodeStatus.RETRYING);
                        Duration delay = retryPolicy.getDelay(attempt);
                        return CompletableFuture
                                .supplyAsync(() -> {
                                    try {
                                        Thread.sleep(delay.toMillis());
                                        return executeWithRetry(context, attempt + 1);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        return CompletableFuture.completedFuture(
                                                NodeResult.failure(id, "Interrupted during retry"));
                                    }
                                })
                                .thenCompose(Function.identity());
                    } else {
                        setStatus(NodeStatus.FAILED);
                        return CompletableFuture.completedFuture(result);
                    }
                })
                .exceptionally(throwable -> {
                    setStatus(NodeStatus.FAILED);
                    return NodeResult.failure(id, throwable.getMessage());
                });
    }

    /**
     * Sets the node status and emits event.
     */
    public void setStatus(NodeStatus status) {
        NodeStatus oldStatus = this.status;
        this.status = status;
        // Emit status change event
        if (eventBus != null) {
            eventBus.publish(new NodeStatusChangedEvent(id, oldStatus, status));
        }
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Functional interface for node execution.
     */
    @FunctionalInterface
    public interface NodeExecutor {
        CompletableFuture<NodeResult> execute(ExecutionNode node, ExecutionContext context);
    }

    public static class Builder {
        private UUID id;
        private String type;
        private String name;
        private NodeConfig config;
        private Duration timeout;
        private RetryPolicy retryPolicy;
        private boolean parallelizable;
        private ResourceRequirements resourceRequirements;
        private NodeExecutor executor;
        private EventBus eventBus;

        private Builder() {
        }

        private Builder(DefaultExecutionNode node) {
            this.id = node.id;
            this.type = node.type;
            this.name = node.name.orElse(null);
            this.config = node.config;
            this.timeout = node.timeout;
            this.retryPolicy = node.retryPolicy;
            this.parallelizable = node.parallelizable;
            this.resourceRequirements = node.resourceRequirements;
            this.executor = node.executor;
            this.eventBus = node.eventBus;
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder config(NodeConfig config) {
            this.config = config;
            return this;
        }

        public Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder retryPolicy(RetryPolicy retryPolicy) {
            this.retryPolicy = retryPolicy;
            return this;
        }

        public Builder parallelizable(boolean parallelizable) {
            this.parallelizable = parallelizable;
            return this;
        }

        public Builder resourceRequirements(ResourceRequirements resourceRequirements) {
            this.resourceRequirements = resourceRequirements;
            return this;
        }

        public Builder executor(NodeExecutor executor) {
            this.executor = executor;
            return this;
        }

        public Builder eventBus(EventBus eventBus) {
            this.eventBus = eventBus;
            return this;
        }

        public DefaultExecutionNode build() {
            return new DefaultExecutionNode(this);
        }
    }
}
