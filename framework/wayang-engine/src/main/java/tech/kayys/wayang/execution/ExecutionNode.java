package tech.kayys.wayang.execution;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;



/**
 * Represents a single executable unit within an ExecutionGraph.
 * 
 * <p>
 * Nodes are the building blocks of execution. They can represent:
 * <ul>
 * <li>AI operations (prompts, inference, reflection)</li>
 * <li>Workflow operations (conditions, loops, parallelism)</li>
 * <li>Tool operations (HTTP, database, shell, MCP)</li>
 * <li>Control operations (wait, delay, approval)</li>
 * <li>Plugin operations (any custom node type)</li>
 * </ul>
 * 
 * <p>
 * Nodes have no inherent knowledge of execution order or dependencies.
 * Those are defined by the edges connecting nodes.
 */
public interface ExecutionNode {

    /**
     * Returns the unique identifier of this node.
     */
    UUID id();

    /**
     * Returns the type of this node (e.g., "prompt", "tool", "condition").
     */
    String type();

    /**
     * Returns the name of this node (optional).
     */
    Optional<String> name();

    /**
     * Returns the current status of this node.
     */
    NodeStatus status();

    /**
     * Returns the incoming edges to this node.
     */
    Collection<ExecutionEdge> incomingEdges();

    /**
     * Returns the outgoing edges from this node.
     */
    Collection<ExecutionEdge> outgoingEdges();

    /**
     * Executes this node with the given context.
     * 
     * @param context The execution context
     * @return A CompletableFuture that completes with the node result
     */
    CompletableFuture<NodeResult> execute(ExecutionContext context);

    /**
     * Returns the configuration for this node.
     */
    NodeConfig config();

    /**
     * Returns the timeout for this node execution.
     */
    java.time.Duration timeout();

    /**
     * Returns the retry policy for this node.
     */
    RetryPolicy retryPolicy();

    /**
     * Checks if this node can be executed in parallel.
     */
    boolean isParallelizable();


}
