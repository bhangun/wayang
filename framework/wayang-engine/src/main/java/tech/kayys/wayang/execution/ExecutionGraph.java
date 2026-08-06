package tech.kayys.wayang.execution;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * A Directed Acyclic Graph (DAG) representing an executable workflow.
 * 
 * <p>
 * The ExecutionGraph is the core data structure for all executable
 * components in Wayang. It unifies workflows, agents, and tools under
 * a single execution model.
 * 
 * <p>
 * Key characteristics:
 * <ul>
 * <li>Nodes represent executable units (prompts, tools, conditions, etc.)</li>
 * <li>Edges represent dependencies and control flow</li>
 * <li>Conditional branching based on node outcomes</li>
 * <li>Support for parallel execution of independent branches</li>
 * <li>Loop structures through graph cycles with safeguards</li>
 * </ul>
 * 
 * @see ExecutionNode
 * @see ExecutionEdge
 */
public interface ExecutionGraph {

    /**
     * Returns the unique identifier of this graph.
     */
    UUID id();

    /**
     * Returns the name of this graph (optional).
     */
    Optional<String> name();

    /**
     * Returns all nodes in this graph.
     */
    Collection<ExecutionNode> nodes();

    /**
     * Returns all edges in this graph.
     */
    Collection<ExecutionEdge> edges();

    /**
     * Finds a node by its ID.
     */
    Optional<ExecutionNode> findNode(UUID nodeId);

    /**
     * Returns the start node (node with no incoming edges).
     */
    Optional<ExecutionNode> getStartNode();

    /**
     * Returns the end nodes (nodes with no outgoing edges).
     */
    Collection<ExecutionNode> getEndNodes();

    /**
     * Returns the metadata for this graph.
     */
    ExecutionMetadata metadata();

    /**
     * Validates the graph structure (no cycles, valid start/end).
     */
    boolean validate();
}
