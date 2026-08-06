package tech.kayys.wayang.execution;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents a directed edge between two nodes in an ExecutionGraph.
 * 
 * <p>
 * Edges define dependencies and control flow between nodes.
 * Each edge can have a condition that determines whether the edge
 * should be traversed.
 * 
 * <p>
 * Edge conditions enable:
 * <ul>
 * <li>Conditional branching (success/failure)</li>
 * <li>Expression-based routing</li>
 * <li>Human approval requirements</li>
 * <li>Probabilistic execution</li>
 * </ul>
 */
public interface ExecutionEdge {

    /**
     * Returns the source node ID.
     */
    UUID from();

    /**
     * Returns the target node ID.
     */
    UUID to();

    /**
     * Returns the condition for this edge.
     */
    EdgeCondition condition();

    /**
     * Returns the label for this edge (optional).
     */
    Optional<String> label();

    /**
     * Returns the weight of this edge (for priority scheduling).
     */
    default double weight() {
        return 1.0;
    }
}
