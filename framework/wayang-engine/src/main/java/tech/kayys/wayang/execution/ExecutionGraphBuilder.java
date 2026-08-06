package tech.kayys.wayang.execution;

import java.util.*;

import tech.kayys.wayang.execution.impl.DefaultExecutionGraph;
import tech.kayys.wayang.execution.impl.DefaultExecutionNode;

/**
 * Builder for creating ExecutionGraph instances with a fluent API.
 * 
 * <p>
 * Provides a convenient way to construct execution graphs
 * with nodes, edges, and metadata.
 */
public final class ExecutionGraphBuilder {

    private UUID id;
    private String name;
    private final List<ExecutionNode> nodes;
    private final List<ExecutionEdge> edges;
    private ExecutionMetadata metadata;
    private final Map<String, Object> variables;

    public ExecutionGraphBuilder() {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.variables = new HashMap<>();
        this.metadata = ExecutionMetadata.empty();
    }

    public ExecutionGraphBuilder id(UUID id) {
        this.id = id;
        return this;
    }

    public ExecutionGraphBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ExecutionGraphBuilder node(ExecutionNode node) {
        this.nodes.add(node);
        return this;
    }

    public ExecutionGraphBuilder nodes(Collection<ExecutionNode> nodes) {
        this.nodes.addAll(nodes);
        return this;
    }

    public ExecutionGraphBuilder edge(ExecutionEdge edge) {
        this.edges.add(edge);
        return this;
    }

    public ExecutionGraphBuilder edge(ExecutionNode from, ExecutionNode to, EdgeCondition condition) {
        ExecutionEdge edge = new DefaultExecutionEdge(from.id(), to.id(), condition);
        this.edges.add(edge);
        return this;
    }

    public ExecutionGraphBuilder edge(ExecutionNode from, ExecutionNode to) {
        return edge(from, to, EdgeCondition.always());
    }

    public ExecutionGraphBuilder edges(Collection<ExecutionEdge> edges) {
        this.edges.addAll(edges);
        return this;
    }

    public ExecutionGraphBuilder metadata(ExecutionMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public ExecutionGraphBuilder variable(String key, Object value) {
        this.variables.put(key, value);
        return this;
    }

    public ExecutionGraphBuilder variables(Map<String, Object> variables) {
        this.variables.putAll(variables);
        return this;
    }

    public ExecutionGraphBuilder createStartNode() {
        ExecutionNode start = new DefaultExecutionNode.Builder()
                .type("start")
                .name("Start")
                .build();
        return node(start);
    }

    public ExecutionGraphBuilder createEndNode() {
        ExecutionNode end = new DefaultExecutionNode.Builder()
                .type("end")
                .name("End")
                .build();
        return node(end);
    }

    public ExecutionGraphBuilder createPromptNode(String name, String prompt) {
        ExecutionNode node = new DefaultExecutionNode.Builder()
                .type("prompt")
                .name(name)
                .config(NodeConfig.builder()
                        .put("prompt", prompt)
                        .build())
                .build();
        return node(node);
    }

    public ExecutionGraphBuilder createToolNode(String name, String toolName, Object parameters) {
        ExecutionNode node = new DefaultExecutionNode.Builder()
                .type("tool")
                .name(name)
                .config(NodeConfig.builder()
                        .put("tool", toolName)
                        .put("parameters", parameters)
                        .build())
                .build();
        return node(node);
    }

    public ExecutionGraphBuilder createConditionNode(String name, String expression) {
        ExecutionNode node = new DefaultExecutionNode.Builder()
                .type("condition")
                .name(name)
                .config(NodeConfig.builder()
                        .put("expression", expression)
                        .build())
                .build();
        return node(node);
    }

    public ExecutionGraphBuilder createApprovalNode(String name, String description) {
        ExecutionNode node = new DefaultExecutionNode.Builder()
                .type("approval")
                .name(name)
                .config(NodeConfig.builder()
                        .put("description", description)
                        .build())
                .build();
        return node(node);
    }

    public ExecutionGraph build() {
        return new DefaultExecutionGraph.Builder()
                .id(id != null ? id : UUID.randomUUID())
                .name(name)
                .nodes(nodes)
                .edges(edges)
                .metadata(metadata)
                .build();
    }

    public static ExecutionGraphBuilder builder() {
        return new ExecutionGraphBuilder();
    }
}