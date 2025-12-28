package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Business workflow details with HITL tasks.
 */
public class BusinessWorkflowDetails {
    private String runId;
    private String workflowId;
    private String status;
    private List<HumanTaskSummary> pendingTasks;
    private List<HumanTaskSummary> completedTasks;
    private Integer slaHoursRemaining;
    private Instant slaDeadline;
    private Map<String, Object> businessData;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<HumanTaskSummary> getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(List<HumanTaskSummary> pendingTasks) {
        this.pendingTasks = pendingTasks;
    }

    public List<HumanTaskSummary> getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(List<HumanTaskSummary> completedTasks) {
        this.completedTasks = completedTasks;
    }

    public Integer getSlaHoursRemaining() {
        return slaHoursRemaining;
    }

    public void setSlaHoursRemaining(Integer hours) {
        this.slaHoursRemaining = hours;
    }

    public Instant getSlaDeadline() {
        return slaDeadline;
    }

    public void setSlaDeadline(Instant deadline) {
        this.slaDeadline = deadline;
    }

    public Map<String, Object> getBusinessData() {
        return businessData;
    }

    public void setBusinessData(Map<String, Object> businessData) {
        this.businessData = businessData;
    }
}
