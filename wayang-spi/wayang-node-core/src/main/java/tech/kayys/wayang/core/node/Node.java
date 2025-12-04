package tech.kayys.wayang.core.node;

import tech.kayys.wayang.core.exception.NodeException;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

import com.networknt.schema.ValidationResult;

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
     * Unique node identifier
     */
    String getNodeId();
    
    /**
     * Initialize the node with its descriptor and configuration.
     * 
     * This method is called once when the node is loaded into the runtime.
     * Use this for:
     * - Validating configuration
     * - Initializing resources (connections, caches, etc.)
     * - Setting up internal state
     * 
     * @param descriptor The node descriptor containing metadata and schema
     * @param config Configuration properties specific to this node instance
     * @throws NodeException if initialization fails
     */
    void onLoad(NodeDescriptor descriptor, NodeConfig config) throws NodeException;
    
    /**
     * Execute the node's logic with the given context.
     * 
     * This method contains the core business logic of the node.
     * It should be idempotent when possible to support retries.
     * 
     * For long-running operations, consider:
     * - Implementing checkpointing
     * - Honoring context deadline
     * - Emitting progress events
     * 
     * @param context The execution context containing inputs, services, and metadata
     * @return CompletionStage with the execution result
     * @throws NodeException if execution fails unrecoverably
     */
    CompletionStage<ExecutionResult> execute(NodeContext context) throws NodeException;
    
    /**
     * Clean up resources before the node is unloaded.
     * 
     * This method is called once when the node is being removed from the runtime.
     * Use this for:
     * - Closing connections
     * - Flushing caches
     * - Releasing resources
     * 
     * Implementations should not throw exceptions.
     */
    void onUnload();
    
    /**
     * Get the descriptor for this node.
     * Node descriptor containing metadata and schema
     * 
     * @return The node descriptor
     */
    NodeDescriptor getDescriptor();
    
    /**
     * Check if this node supports streaming execution.
     * 
     * @return true if the node can stream partial results
     */
    default boolean supportsStreaming() {
        return false;
    }
    
    /**
     * Check if this node supports checkpointing for long-running operations.
     * 
     * @return true if the node can create checkpoints
     */
    default boolean supportsCheckpointing() {
        return false;
    }
    
    /**
     * Validate inputs before execution.
     * 
     * This is called before execute() to fail fast on invalid inputs.
     * 
     * @param context The execution context
     * @throws NodeException if validation fails
     */
    default void validateInputs(NodeContext context) throws NodeException {
        // Default implementation does nothing
    }


    /**
     * Validate node configuration and inputs
     */
    ValidationResult validate(NodeContext context);

 

  

    
    /**
     * Optional: Checkpoint current state for resumability
     * @param context Current execution context
     * @return Checkpoint state or empty if not supported
     */
    default Optional<Object> checkpoint(NodeContext context) {
        return Optional.empty();
    }
    
    /**
     * Optional: Resume from checkpoint
     * @param context Execution context
     * @param checkpoint Previous checkpoint state
     * @return Execution result continuing from checkpoint
     */
    default CompletionStage<ExecutionResult> resume(NodeContext context, Object checkpoint) throws Exception {
        return execute(context);
    }
    
    /**
     * Health check - verify node can execute
     */
    default Boolean healthCheck() {
        return true;
    }
}
