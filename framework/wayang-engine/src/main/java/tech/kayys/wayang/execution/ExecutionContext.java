package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The execution context provided to nodes during execution.
 * 
 * <p>
 * Contains all the information a node needs to execute:
 * <ul>
 * <li>The current agent session</li>
 * <li>The execution graph</li>
 * <li>Variables and state</li>
 * <li>Resources (tools, memory, RAG)</li>
 * <li>Runtime services</li>
 * </ul>
 */
public interface ExecutionContext {

    /**
     * Returns the execution graph.
     */
    ExecutionGraph graph();

    /**
     * Returns the variable store.
     */
    VariableStore variables();

    /**
     * Returns the resource context.
     */
    ResourceContext resources();

    /**
     * Returns the current node being executed.
     */
    ExecutionNode currentNode();

    /**
     * Returns the parent execution context (if any).
     */
    Optional<ExecutionContext> parent();

    /**
     * Returns the execution ID.
     */
    UUID executionId();

    /**
     * Returns the execution metadata.
     */
    ExecutionMetadata metadata();

    /**
     * Creates a child context for sub-execution.
     */
    ExecutionContext createChild(Map<String, Object> variables);
}
