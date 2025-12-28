package tech.kayys.wayang.node.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Node Type Usage Statistics.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NodeTypeStats {

    private String nodeTypeId;
    private long totalExecutions;
    private long successfulExecutions;
    private long failedExecutions;
    private double successRate;
    private long avgExecutionTime;
    private Map<String, Long> executionsByTenant;
    private Map<String, Long> errorsByType;

    public NodeTypeStats() {
    }

    // Getters and setters
    public String getNodeTypeId() {
        return nodeTypeId;
    }

    public void setNodeTypeId(String nodeTypeId) {
        this.nodeTypeId = nodeTypeId;
    }

    public long getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(long totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public void setSuccessfulExecutions(long successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }

    public long getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(long failedExecutions) {
        this.failedExecutions = failedExecutions;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public long getAvgExecutionTime() {
        return avgExecutionTime;
    }

    public void setAvgExecutionTime(long avgExecutionTime) {
        this.avgExecutionTime = avgExecutionTime;
    }

    public Map<String, Long> getExecutionsByTenant() {
        return executionsByTenant;
    }

    public void setExecutionsByTenant(Map<String, Long> executionsByTenant) {
        this.executionsByTenant = executionsByTenant;
    }

    public Map<String, Long> getErrorsByType() {
        return errorsByType;
    }

    public void setErrorsByType(Map<String, Long> errorsByType) {
        this.errorsByType = errorsByType;
    }
}
