package tech.kayys.wayang.agent.orchestration.graph.edge;

import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;

/**
 * Defines conditional routing logic between nodes in the graph.
 */
public interface ConditionalEdge {

    /**
     * Determines the name of the next node to execute based on the current state.
     * 
     * @param state the current graph state
     * @return the name of the next node, or "__END__" to terminate the workflow
     */
    String next(GraphState state);
}
