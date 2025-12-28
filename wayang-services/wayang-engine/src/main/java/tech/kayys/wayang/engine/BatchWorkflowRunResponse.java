package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.List;

/**
 * Batch workflow run response.
 */
public class BatchWorkflowRunResponse {
    private String batchId;
    private String workflowId;
    private Integer totalChunks;
    private Integer completedChunks;
    private Integer failedChunks;
    private String status; // RUNNING, COMPLETED, PARTIALLY_FAILED, FAILED
    private List<ChunkExecutionSummary> chunks;
    private Instant startedAt;
    private Instant completedAt;

    // Getters and setters
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public Integer getTotalChunks() {
        return totalChunks;
    }

    public void setTotalChunks(Integer totalChunks) {
        this.totalChunks = totalChunks;
    }

    public Integer getCompletedChunks() {
        return completedChunks;
    }

    public void setCompletedChunks(Integer completedChunks) {
        this.completedChunks = completedChunks;
    }

    public Integer getFailedChunks() {
        return failedChunks;
    }

    public void setFailedChunks(Integer failedChunks) {
        this.failedChunks = failedChunks;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<ChunkExecutionSummary> getChunks() {
        return chunks;
    }

    public void setChunks(List<ChunkExecutionSummary> chunks) {
        this.chunks = chunks;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}
