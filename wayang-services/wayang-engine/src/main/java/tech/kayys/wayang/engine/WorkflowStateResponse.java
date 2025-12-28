package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.Map;

/**
 * Workflow state response with node execution details.
 */
public class WorkflowStateResponse {
    private String runId;
    private String status; // PENDING, RUNNING, WAITING, SUCCEEDED, FAILED, CANCELLED
    private Map<String, NodeExecutionState> nodeStates;
    private Map<String, Object> variables;
    private String currentNodeId;
    private Instant lastUpdated;

    // Getters and setters
    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, NodeExecutionState> getNodeStates() {
        return nodeStates;
    }

    public void setNodeStates(Map<String, NodeExecutionState> nodeStates) {
        this.nodeStates = nodeStates;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String nodeId) {
        this.currentNodeId = nodeId;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
