package tech.kayys.wayang.agent.executor;

import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.engine.node.NodeExecutionResult;
import tech.kayys.gamelan.engine.node.NodeExecutionTask;
import tech.kayys.gamelan.sdk.executor.core.WorkflowExecutor;
import tech.kayys.wayang.agent.AgentType;
import tech.kayys.wayang.error.ErrorCode;
import tech.kayys.wayang.error.WayangException;

import java.time.Instant;
import java.util.Map;

/**
 * Abstract base class for all agent executors
 * Implements the WorkflowExecutor interface from gamelan-sdk-executor-core
 */
public abstract class AbstractAgentExecutor implements WorkflowExecutor {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Execute a node task by delegating to agent-specific implementation
     */
    @Override
    public Uni<NodeExecutionResult> execute(NodeExecutionTask task) {
        logger.info("Executing task: runId={}, nodeId={}, executorType={}",
                task.runId(), task.nodeId(), getExecutorType());

        return beforeExecute(task)
                .chain(() -> doExecute(task))
                .invoke(result -> logger.info("Task completed successfully: runId={}, nodeId={}",
                        task.runId(), task.nodeId()))
                .call(result -> afterExecute(task, result))
                .onFailure().invoke(error -> {
                    logger.error("Task execution failed: runId={}, nodeId={}, error={}",
                            task.runId(), task.nodeId(), error.getMessage(), error);
                    onError(task, error).subscribe().with(
                            v -> {
                            },
                            e -> logger.error("Error in onError handler", e));
                });
    }

    /**
     * Agent-specific execution logic
     * Subclasses must implement this method
     * 
     * @param task The task to execute
     * @return Result of execution
     */
    protected abstract Uni<NodeExecutionResult> doExecute(NodeExecutionTask task);

    /**
     * Get the agent type this executor handles
     */
    protected abstract AgentType getAgentType();

    /**
     * Validate if this executor can handle the task
     * Default implementation checks if task contains required agent configuration
     */
    @Override
    public boolean canHandle(NodeExecutionTask task) {
        Map<String, Object> context = task.context();

        // Check if task has agent configuration
        if (!context.containsKey("agentType")) {
            return false;
        }

        String taskAgentType = (String) context.get("agentType");
        return getExecutorType().equals(taskAgentType);
    }

    /**
     * Get supported node types
     * By default, supports all node types
     */
    @Override
    public String[] getSupportedNodeTypes() {
        return new String[] { "agent-task", "agent-coordination" };
    }

    @jakarta.inject.Inject
    protected tech.kayys.wayang.memory.spi.AgentMemory agentMemory;

    @jakarta.inject.Inject
    protected tech.kayys.wayang.tool.spi.ToolExecutor toolExecutor;

    /**
     * Called before execution starts
     * Can be overridden for custom pre-execution logic
     */
    @Override
    public Uni<Void> beforeExecute(NodeExecutionTask task) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Called after execution completes (success or failure)
     * Stores execution result in agent memory.
     */
    @Override
    public Uni<Void> afterExecute(NodeExecutionTask task, NodeExecutionResult result) {
        if (result.status() == tech.kayys.gamelan.engine.node.NodeExecutionStatus.COMPLETED) {
            String agentId = (String) task.context().get("agentId");
            // If agentId is not explicit, we might use runId or nodeId as proxy, or skip
            if (agentId != null && agentMemory != null) {
                String content = result.output().toString(); // Naive serialization
                if (result.output().containsKey("content")) {
                    content = String.valueOf(result.output().get("content"));
                } else if (result.output().containsKey("response")) {
                    content = String.valueOf(result.output().get("response"));
                }

                tech.kayys.wayang.memory.spi.MemoryEntry entry = new tech.kayys.wayang.memory.spi.MemoryEntry(
                        null,
                        content,
                        Instant.now(),
                        task.context());

                return agentMemory.store(agentId, entry);
            }
        }
        return Uni.createFrom().voidItem();
    }

    /**
     * Called when execution fails with an exception
     * Can be overridden for custom error handling
     */
    @Override
    public Uni<Void> onError(NodeExecutionTask task, Throwable error) {
        return Uni.createFrom().voidItem();
    }

    /**
     * Initialize the executor
     * Can be overridden for custom initialization
     */
    @Override
    public Uni<Void> initialize() {
        logger.info("Initializing executor: {}", getExecutorType());
        return Uni.createFrom().voidItem();
    }

    /**
     * Cleanup the executor
     * Can be overridden for custom cleanup
     */
    @Override
    public Uni<Void> cleanup() {
        logger.info("Cleaning up executor: {}", getExecutorType());
        return Uni.createFrom().voidItem();
    }

    /**
     * Check if the executor is ready
     */
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * Get maximum concurrent tasks
     * Can be overridden by subclasses
     */
    @Override
    public int getMaxConcurrentTasks() {
        return 10; // Default to 10 concurrent tasks
    }

    /**
     * Helper method to create a success result
     */
    protected NodeExecutionResult createSuccessResult(
            NodeExecutionTask task,
            Map<String, Object> output) {
        return new tech.kayys.gamelan.engine.node.DefaultNodeExecutionResult(
                task.runId(),
                task.nodeId(),
                task.attempt(),
                tech.kayys.gamelan.engine.node.NodeExecutionStatus.COMPLETED,
                output,
                null,
                task.token());
    }

    /**
     * Helper method to create a failure result
     */
    protected NodeExecutionResult createFailureResult(
            NodeExecutionTask task,
            Throwable error) {
        return new tech.kayys.gamelan.engine.node.DefaultNodeExecutionResult(
                task.runId(),
                task.nodeId(),
                task.attempt(),
                tech.kayys.gamelan.engine.node.NodeExecutionStatus.FAILED,
                Map.of(),
                toErrorInfo(error),
                task.token());
    }

    private tech.kayys.gamelan.engine.error.ErrorInfo toErrorInfo(Throwable error) {
        if (error instanceof WayangException we) {
            ErrorCode errorCode = we.getErrorCode();
            return new tech.kayys.gamelan.engine.error.ErrorInfo(
                    errorCode.getCode(),
                    we.getMessage(),
                    stackTrace(error),
                    Map.of("retryable", errorCode.isRetryable()));
        }
        ErrorCode errorCode = ErrorCode.RUNTIME_ERROR;
        return new tech.kayys.gamelan.engine.error.ErrorInfo(
                errorCode.getCode(),
                error.getMessage(),
                stackTrace(error),
                Map.of("retryable", errorCode.isRetryable()));
    }

    private static String stackTrace(Throwable throwable) {
        var sw = new java.io.StringWriter();
        throwable.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }
}
