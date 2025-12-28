package tech.kayys.wayang.engine;

/**
 * Per-node metrics.
 */
public class NodeMetrics {
    private String nodeId;
    private Long executionCount;
    private Long successCount;
    private Long failureCount;
    private Double averageDurationMs;
    private Double errorRate;

    // Getters and setters
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Long getExecutionCount() {
        return executionCount;
    }

    public void setExecutionCount(Long executionCount) {
        this.executionCount = executionCount;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(Long failureCount) {
        this.failureCount = failureCount;
    }

    public Double getAverageDurationMs() {
        return averageDurationMs;
    }

    public void setAverageDurationMs(Double averageDurationMs) {
        this.averageDurationMs = averageDurationMs;
    }

    public Double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(Double errorRate) {
        this.errorRate = errorRate;
    }
}
