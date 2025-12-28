package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Workflow run query for advanced filtering.
 */
public class WorkflowRunQuery {
    private String workflowId;
    private String tenantId;
    private List<String> statuses;
    private Instant startTimeFrom;
    private Instant startTimeTo;
    private String triggeredBy;
    private Map<String, String> metadataFilters;
    private Integer page = 0;
    private Integer size = 20;
    private String sortBy = "startedAt";
    private String sortDirection = "DESC";

    // Getters and setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public List<String> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }

    public Instant getStartTimeFrom() {
        return startTimeFrom;
    }

    public void setStartTimeFrom(Instant startTimeFrom) {
        this.startTimeFrom = startTimeFrom;
    }

    public Instant getStartTimeTo() {
        return startTimeTo;
    }

    public void setStartTimeTo(Instant startTimeTo) {
        this.startTimeTo = startTimeTo;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Map<String, String> getMetadataFilters() {
        return metadataFilters;
    }

    public void setMetadataFilters(Map<String, String> metadataFilters) {
        this.metadataFilters = metadataFilters;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
