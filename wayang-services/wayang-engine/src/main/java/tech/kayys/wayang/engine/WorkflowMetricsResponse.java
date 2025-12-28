package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.Map;

/**
 * Workflow metrics response.
 */
public class WorkflowMetricsResponse {
    private String workflowId;
    private Long totalExecutions;
    private Long successfulExecutions;
    private Long failedExecutions;
    private Double averageDurationMs;
    private Double p95DurationMs;
    private Double p99DurationMs;
    private Map<String, NodeMetrics> nodeMetrics;
    private Instant fromTime;
    private Instant toTime;

    // Getters and setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Long getTotalExecutions() {
        return totalExecutions;
    }

    public void setTotalExecutions(Long totalExecutions) {
        this.totalExecutions = totalExecutions;
    }

    public Long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public void setSuccessfulExecutions(Long successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }

    public Long getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(Long failedExecutions) {
        this.failedExecutions = failedExecutions;
    }

    public Double getAverageDurationMs() {
        return averageDurationMs;
    }

    public void setAverageDurationMs(Double averageDurationMs) {
        this.averageDurationMs = averageDurationMs;
    }

    public Double getP95DurationMs() {
        return p95DurationMs;
    }

    public void setP95DurationMs(Double p95DurationMs) {
        this.p95DurationMs = p95DurationMs;
    }

    public Double getP99DurationMs() {
        return p99DurationMs;
    }

    public void setP99DurationMs(Double p99DurationMs) {
        this.p99DurationMs = p99DurationMs;
    }

    public Map<String, NodeMetrics> getNodeMetrics() {
        return nodeMetrics;
    }

    public void setNodeMetrics(Map<String, NodeMetrics> nodeMetrics) {
        this.nodeMetrics = nodeMetrics;
    }

    public Instant getFromTime() {
        return fromTime;
    }

    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }

    public Instant getToTime() {
        return toTime;
    }

    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }
}
