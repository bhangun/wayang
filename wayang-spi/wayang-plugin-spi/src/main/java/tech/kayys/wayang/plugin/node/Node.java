package tech.kayys.wayang.plugin.node;

import java.util.Optional;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.plugin.ExecutionResult;

/**
 * Base interface for all executable nodes in the Wayang platform.
 * Follows the triplet pattern: inputs → execute → outputs (success, error)
 * 
 * All implementations must be:
 * - Idempotent (safe to retry)
 * - Thread-safe
 * - Resource-aware (respect quotas)
 * - Observable (emit metrics/traces)
 */
public interface Node {
    
    /**
     * Lifecycle hook: called once when node is loaded into runtime
     * @param descriptor The immutable node descriptor
     * @param config Runtime configuration
     */
    void onLoad(NodeDescriptor descriptor, NodeConfig config) throws NodeException;
    
    /**
     * Main execution method - MUST be idempotent
     * @param context Execution context with inputs, variables, metadata
     * @return Execution result with outputs and status
     */
    Uni<ExecutionResult> execute(NodeContext context) throws NodeExecutionException;
    
    /**
     * Lifecycle hook: called when node is unloaded from runtime
     */
    void onUnload();
    
    /**
     * Optional: Checkpoint current state for resumability
     * @param context Current execution context
     * @return Checkpoint state or empty if not supported
     */
    default Optional<CheckpointState> checkpoint(NodeContext context) {
        return Optional.empty();
    }
    
    /**
     * Optional: Resume from checkpoint
     * @param context Execution context
     * @param checkpoint Previous checkpoint state
     * @return Execution result continuing from checkpoint
     * @throws NodeExecutionException 
     */
    default Uni<ExecutionResult> resume(NodeContext context, CheckpointState checkpoint) throws NodeExecutionException {
        return execute(context);
    }
    
    /**
     * Health check - verify node can execute
     */
    default Uni<HealthStatus> healthCheck() {
        return Uni.createFrom().item(HealthStatus.healthy());
    }
}

// Minimal NodeException so the interface compiles when the referenced type is not provided elsewhere.
class NodeException extends Exception {
    private static final long serialVersionUID = 1L;

    public NodeException() {
        super();
    }

    public NodeException(String message) {
        super(message);
    }

    public NodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeException(Throwable cause) {
        super(cause);
    }
}

// Minimal NodeExecutionException so the interface compiles when the referenced type is not provided elsewhere.
class NodeExecutionException extends Exception {
    private static final long serialVersionUID = 1L;

    public NodeExecutionException() {
        super();
    }

    public NodeExecutionException(String message) {
        super(message);
    }

    public NodeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeExecutionException(Throwable cause) {
        super(cause);
    }
}
