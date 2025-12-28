package tech.kayys.wayang.schema;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Workflow Run Summary.
 * 
 * Lightweight summary of a workflow run for list views.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowRunSummary {

    private String runId;
    private String workflowId;
    private String workflowName;
    private String status;
    private String startTime;
    private String endTime;
    private Long duration;
    private String triggeredBy;
    private int totalNodes;
    private int completedNodes;
    private int failedNodes;

    public WorkflowRunSummary() {
    }

    // Getters and setters
    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    public int getCompletedNodes() {
        return completedNodes;
    }

    public void setCompletedNodes(int completedNodes) {
        this.completedNodes = completedNodes;
    }

    public int getFailedNodes() {
        return failedNodes;
    }

    public void setFailedNodes(int failedNodes) {
        this.failedNodes = failedNodes;
    }
}
