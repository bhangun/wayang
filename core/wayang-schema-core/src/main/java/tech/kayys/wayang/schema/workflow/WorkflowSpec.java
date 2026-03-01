package tech.kayys.wayang.schema.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import tech.kayys.wayang.schema.node.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorkflowSpec — Logical workflow definition within a WayangSpec.
 * <p>
 * Contains the execution graph (nodes + connections), variables,
 * and workflow-level configuration (timeout, retry, circuit breaker).
 */
public class WorkflowSpec {

    @JsonProperty("nodes")
    private List<Node> nodes = new ArrayList<>();

    @JsonProperty("connections")
    private List<Connection> connections = new ArrayList<>();

    @JsonProperty("variables")
    private Map<String, Object> variables = new HashMap<>();

    @JsonProperty("configuration")
    private WorkflowConfig configuration;

    @JsonProperty("entryNodeId")
    private String entryNodeId;

    @JsonProperty("exitNodeIds")
    private List<String> exitNodeIds = new ArrayList<>();

    public WorkflowSpec() {
        // Default constructor for JSON deserialization
    }

    public WorkflowSpec(List<Node> nodes, List<Connection> connections,
            Map<String, Object> variables, WorkflowConfig configuration) {
        this.nodes = nodes != null ? nodes : new ArrayList<>();
        this.connections = connections != null ? connections : new ArrayList<>();
        this.variables = variables != null ? variables : new HashMap<>();
        this.configuration = configuration;
    }

    // Getters
    public List<Node> getNodes() {
        return nodes;
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public WorkflowConfig getConfiguration() {
        return configuration;
    }

    public String getEntryNodeId() {
        return entryNodeId;
    }

    public List<String> getExitNodeIds() {
        return exitNodeIds;
    }

    // Setters
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void setConfiguration(WorkflowConfig configuration) {
        this.configuration = configuration;
    }

    public void setEntryNodeId(String entryNodeId) {
        this.entryNodeId = entryNodeId;
    }

    public void setExitNodeIds(List<String> exitNodeIds) {
        this.exitNodeIds = exitNodeIds;
    }
}
