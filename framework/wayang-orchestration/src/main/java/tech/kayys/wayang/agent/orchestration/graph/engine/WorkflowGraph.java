package tech.kayys.wayang.agent.orchestration.graph.engine;

import tech.kayys.wayang.agent.orchestration.graph.checkpoint.CheckpointStrategy;
import tech.kayys.wayang.agent.orchestration.graph.checkpoint.InMemoryCheckpointStrategy;
import tech.kayys.wayang.agent.orchestration.graph.edge.ConditionalEdge;
import tech.kayys.wayang.agent.orchestration.graph.node.GraphNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Builder for constructing a Multi-Agent State Machine Workflow.
 */
public class WorkflowGraph {

    private final Map<String, GraphNode> nodes = new HashMap<>();
    private final Map<String, String> edges = new HashMap<>();
    private final Map<String, ConditionalEdge> conditionalEdges = new HashMap<>();
    
    private String entryPoint;
    private CheckpointStrategy checkpointStrategy = new InMemoryCheckpointStrategy();

    public WorkflowGraph addNode(String name, GraphNode node) {
        nodes.put(name, node);
        return this;
    }

    public WorkflowGraph addEdge(String from, String to) {
        edges.put(from, to);
        return this;
    }

    public WorkflowGraph addConditionalEdge(String from, ConditionalEdge condition) {
        conditionalEdges.put(from, condition);
        return this;
    }

    public WorkflowGraph setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
        return this;
    }

    public WorkflowGraph withCheckpointStrategy(CheckpointStrategy strategy) {
        this.checkpointStrategy = strategy;
        return this;
    }

    public CompiledWorkflow compile() {
        if (entryPoint == null || !nodes.containsKey(entryPoint)) {
            throw new IllegalStateException("Invalid or missing entry point.");
        }
        return new CompiledWorkflow(nodes, edges, conditionalEdges, entryPoint, checkpointStrategy);
    }
}
