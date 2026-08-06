package tech.kayys.wayang.execution.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import tech.kayys.wayang.execution.ExecutionEdge;
import tech.kayys.wayang.execution.ExecutionGraph;
import tech.kayys.wayang.execution.ExecutionMetadata;
import tech.kayys.wayang.execution.ExecutionNode;

/**
 * Default implementation of the ExecutionGraph.
 * 
 * <p>
 * Thread-safe implementation using concurrent collections.
 * Supports dynamic graph modification (useful for conditional execution).
 */
public final class DefaultExecutionGraph implements ExecutionGraph {

    private final UUID id;
    private final Optional<String> name;
    private final Map<UUID, ExecutionNode> nodeMap;
    private final Map<UUID, ExecutionEdge> edgeMap;
    private final ExecutionMetadata metadata;
    private volatile boolean validated;

    private DefaultExecutionGraph(Builder builder) {
        this.id = builder.id != null ? builder.id : UUID.randomUUID();
        this.name = Optional.ofNullable(builder.name);
        this.nodeMap = new ConcurrentHashMap<>();
        this.edgeMap = new ConcurrentHashMap<>();
        this.metadata = builder.metadata != null ? builder.metadata : ExecutionMetadata.empty();
        this.validated = false;

        if (builder.nodes != null) {
            for (ExecutionNode node : builder.nodes) {
                this.nodeMap.put(node.id(), node);
            }
        }

        if (builder.edges != null) {
            for (ExecutionEdge edge : builder.edges) {
                this.edgeMap.put(UUID.randomUUID(), edge);
            }
        }
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public Optional<String> name() {
        return name;
    }

    @Override
    public Collection<ExecutionNode> nodes() {
        return Collections.unmodifiableCollection(nodeMap.values());
    }

    @Override
    public Collection<ExecutionEdge> edges() {
        return Collections.unmodifiableCollection(edgeMap.values());
    }

    @Override
    public ExecutionMetadata metadata() {
        return metadata;
    }

    @Override
    public Optional<ExecutionNode> findNode(UUID nodeId) {
        return Optional.ofNullable(nodeMap.get(nodeId));
    }

    @Override
    public Optional<ExecutionNode> getStartNode() {
        Set<UUID> targetNodeIds = edges().stream()
                .map(ExecutionEdge::to)
                .collect(Collectors.toSet());

        return nodeMap.values().stream()
                .filter(node -> !targetNodeIds.contains(node.id()))
                .findFirst();
    }

    @Override
    public Collection<ExecutionNode> getEndNodes() {
        Set<UUID> sourceNodeIds = edges().stream()
                .map(ExecutionEdge::from)
                .collect(Collectors.toSet());

        return nodeMap.values().stream()
                .filter(node -> !sourceNodeIds.contains(node.id()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean validate() {
        if (validated) {
            return true;
        }

        // Check for cycles
        if (hasCycles()) {
            throw new IllegalStateException("Graph contains cycles");
        }

        // Check for valid start node
        if (!getStartNode().isPresent()) {
            throw new IllegalStateException("No start node found");
        }

        // Check for valid end nodes
        if (getEndNodes().isEmpty()) {
            throw new IllegalStateException("No end nodes found");
        }

        // Validate all nodes have required configuration
        for (ExecutionNode node : nodes()) {
            validateNode(node);
        }

        // Validate all edges reference valid nodes
        for (ExecutionEdge edge : edges()) {
            if (!nodeMap.containsKey(edge.from())) {
                throw new IllegalStateException("Edge references non-existent node: " + edge.from());
            }
            if (!nodeMap.containsKey(edge.to())) {
                throw new IllegalStateException("Edge references non-existent node: " + edge.to());
            }
        }

        validated = true;
        return true;
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    private boolean hasCycles() {
        Set<UUID> visited = new HashSet<>();
        Set<UUID> recursionStack = new HashSet<>();

        for (UUID nodeId : nodeMap.keySet()) {
            if (hasCycle(nodeId, visited, recursionStack)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCycle(UUID nodeId, Set<UUID> visited, Set<UUID> recursionStack) {
        if (recursionStack.contains(nodeId)) {
            return true;
        }
        if (visited.contains(nodeId)) {
            return false;
        }

        visited.add(nodeId);
        recursionStack.add(nodeId);

        for (ExecutionEdge edge : getOutgoingEdges(nodeId)) {
            if (hasCycle(edge.to(), visited, recursionStack)) {
                return true;
            }
        }

        recursionStack.remove(nodeId);
        return false;
    }

    private Collection<ExecutionEdge> getOutgoingEdges(UUID nodeId) {
        return edgeMap.values().stream()
                .filter(edge -> edge.from().equals(nodeId))
                .collect(Collectors.toList());
    }

    private void validateNode(ExecutionNode node) {
        if (node.type() == null || node.type().isEmpty()) {
            throw new IllegalStateException("Node has no type: " + node.id());
        }

        if (node.timeout() == null) {
            throw new IllegalStateException("Node has no timeout: " + node.id());
        }

        if (node.retryPolicy() == null) {
            throw new IllegalStateException("Node has no retry policy: " + node.id());
        }

        if (node.config() == null) {
            throw new IllegalStateException("Node has no configuration: " + node.id());
        }
    }

    /**
     * Adds a node to the graph dynamically.
     */
    public void addNode(ExecutionNode node) {
        validated = false;
        nodeMap.put(node.id(), node);
    }

    /**
     * Adds an edge to the graph dynamically.
     */
    public void addEdge(ExecutionEdge edge) {
        validated = false;
        edgeMap.put(UUID.randomUUID(), edge);
    }

    /**
     * Removes a node and its associated edges.
     */
    public void removeNode(UUID nodeId) {
        validated = false;
        nodeMap.remove(nodeId);
        edgeMap.entrySet().removeIf(entry -> entry.getValue().from().equals(nodeId) ||
                entry.getValue().to().equals(nodeId));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String name;
        private Collection<ExecutionNode> nodes;
        private Collection<ExecutionEdge> edges;
        private ExecutionMetadata metadata;

        private Builder() {
        }

        private Builder(DefaultExecutionGraph graph) {
            this.id = graph.id;
            this.name = graph.name.orElse(null);
            this.nodes = graph.nodeMap.values();
            this.edges = graph.edgeMap.values();
            this.metadata = graph.metadata;
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder nodes(Collection<ExecutionNode> nodes) {
            this.nodes = nodes;
            return this;
        }

        public Builder edges(Collection<ExecutionEdge> edges) {
            this.edges = edges;
            return this;
        }

        public Builder metadata(ExecutionMetadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public DefaultExecutionGraph build() {
            return new DefaultExecutionGraph(this);
        }
    }
}