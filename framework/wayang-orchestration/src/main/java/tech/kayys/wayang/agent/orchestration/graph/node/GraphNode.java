package tech.kayys.wayang.agent.orchestration.graph.node;

import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;
import tech.kayys.wayang.agent.orchestration.graph.state.StateUpdate;

/**
 * Represents a discrete unit of work in the workflow graph.
 */
public interface GraphNode {
    
    /**
     * Executes the node's logic using the current state.
     *
     * @param state the current shared graph state
     * @return the partial state updates produced by this node
     */
    StateUpdate execute(GraphState state);
}
