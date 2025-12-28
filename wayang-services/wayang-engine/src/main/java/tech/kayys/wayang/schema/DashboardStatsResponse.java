package tech.kayys.wayang.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard Statistics Response.
 * 
 * Provides overview statistics for the dashboard.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    private int totalWorkflows;
    private long totalRuns;
    private int activeRuns;
    private double successRate;
    private long avgDuration;
    private List<RecentRun> recentRuns;
    private WorkflowStats workflowStats;
    private ErrorStats errorStats;

    public DashboardStatsResponse() {
        this.recentRuns = new ArrayList<>();
    }

    /**
     * Recent workflow run summary.
     */
    public static class RecentRun {
        private String runId;
        private String workflowName;
        private String status;
        private String startTime;
        private Long duration;

        public RecentRun() {
        }

        // Getters and setters
        public String getRunId() {
            return runId;
        }

        public void setRunId(String runId) {
            this.runId = runId;
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

        public Long getDuration() {
            return duration;
        }

        public void setDuration(Long duration) {
            this.duration = duration;
        }
    }

    /**
     * Workflow statistics.
     */
    public static class WorkflowStats {
        private int published;
        private int draft;
        private int archived;

        public WorkflowStats() {
        }

        // Getters and setters
        public int getPublished() {
            return published;
        }

        public void setPublished(int published) {
            this.published = published;
        }

        public int getDraft() {
            return draft;
        }

        public void setDraft(int draft) {
            this.draft = draft;
        }

        public int getArchived() {
            return archived;
        }

        public void setArchived(int archived) {
            this.archived = archived;
        }
    }

    /**
     * Error statistics.
     */
    public static class ErrorStats {
        private int totalErrors;
        private int selfHealed;
        private int escalated;
        private int pending;

        public ErrorStats() {
        }

        // Getters and setters
        public int getTotalErrors() {
            return totalErrors;
        }

        public void setTotalErrors(int totalErrors) {
            this.totalErrors = totalErrors;
        }

        public int getSelfHealed() {
            return selfHealed;
        }

        public void setSelfHealed(int selfHealed) {
            this.selfHealed = selfHealed;
        }

        public int getEscalated() {
            return escalated;
        }

        public void setEscalated(int escalated) {
            this.escalated = escalated;
        }

        public int getPending() {
            return pending;
        }

        public void setPending(int pending) {
            this.pending = pending;
        }
    }

    // Getters and setters
    public int getTotalWorkflows() {
        return totalWorkflows;
    }

    public void setTotalWorkflows(int totalWorkflows) {
        this.totalWorkflows = totalWorkflows;
    }

    public long getTotalRuns() {
        return totalRuns;
    }

    public void setTotalRuns(long totalRuns) {
        this.totalRuns = totalRuns;
    }

    public int getActiveRuns() {
        return activeRuns;
    }

    public void setActiveRuns(int activeRuns) {
        this.activeRuns = activeRuns;
    }

    public double getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(double successRate) {
        this.successRate = successRate;
    }

    public long getAvgDuration() {
        return avgDuration;
    }

    public void setAvgDuration(long avgDuration) {
        this.avgDuration = avgDuration;
    }

    public List<RecentRun> getRecentRuns() {
        return recentRuns;
    }

    public void setRecentRuns(List<RecentRun> recentRuns) {
        this.recentRuns = recentRuns;
    }

    public WorkflowStats getWorkflowStats() {
        return workflowStats;
    }

    public void setWorkflowStats(WorkflowStats workflowStats) {
        this.workflowStats = workflowStats;
    }

    public ErrorStats getErrorStats() {
        return errorStats;
    }

    public void setErrorStats(ErrorStats errorStats) {
        this.errorStats = errorStats;
    }
}
