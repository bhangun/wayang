package tech.kayys.wayang.agent.orchestration.graph.engine;

import tech.kayys.wayang.agent.orchestration.graph.checkpoint.CheckpointStrategy;
import tech.kayys.wayang.agent.orchestration.graph.edge.ConditionalEdge;
import tech.kayys.wayang.agent.orchestration.graph.node.GraphNode;
import tech.kayys.wayang.agent.orchestration.graph.state.GraphState;
import tech.kayys.wayang.agent.orchestration.graph.state.StateUpdate;

import java.util.Map;

/**
 * The execution engine for a constructed Multi-Agent Graph.
 */
public class CompiledWorkflow {

    public static final String END = "__END__";

    private final Map<String, GraphNode> nodes;
    private final Map<String, String> edges;
    private final Map<String, ConditionalEdge> conditionalEdges;
    private final String entryPoint;
    private final CheckpointStrategy checkpointer;

    CompiledWorkflow(Map<String, GraphNode> nodes, Map<String, String> edges, 
                     Map<String, ConditionalEdge> conditionalEdges, String entryPoint,
                     CheckpointStrategy checkpointer) {
        this.nodes = Map.copyOf(nodes);
        this.edges = Map.copyOf(edges);
        this.conditionalEdges = Map.copyOf(conditionalEdges);
        this.entryPoint = entryPoint;
        this.checkpointer = checkpointer;
    }

    /**
     * Executes the workflow loop until completion.
     * @param initialState the starting state
     * @param threadId a unique identifier for checkpointing (resuming)
     * @return the final computed state
     */
    public GraphState invoke(GraphState initialState, String threadId) {
        
        GraphState state = initialState;
        
        // Attempt to resume from checkpoint
        GraphState savedState = checkpointer.load(threadId);
        if (savedState != null) {
            state = savedState;
        }

        String currentNodeName = state.get("__next_node__");
        if (currentNodeName == null) {
            currentNodeName = entryPoint;
        }

        while (!END.equals(currentNodeName)) {
            GraphNode node = nodes.get(currentNodeName);
            if (node == null) {
                throw new IllegalStateException("Node not found: " + currentNodeName);
            }

            // 1. Execute Node
            StateUpdate update = node.execute(state);
            
            // 2. Apply Update via Reducers
            state.apply(update);

            // 3. Determine Next Node
            if (conditionalEdges.containsKey(currentNodeName)) {
                currentNodeName = conditionalEdges.get(currentNodeName).next(state);
            } else if (edges.containsKey(currentNodeName)) {
                currentNodeName = edges.get(currentNodeName);
            } else {
                currentNodeName = END;
            }

            // 4. Save Checkpoint
            state.apply(new StateUpdate().put("__next_node__", currentNodeName));
            checkpointer.save(threadId, state);
        }

        return state;
    }
}
