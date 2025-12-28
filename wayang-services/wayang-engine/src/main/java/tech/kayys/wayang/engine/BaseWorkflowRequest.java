package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.*;

/**
 * Base class for all workflow execution requests.
 * Ensures consistent multi-tenancy, security, and audit tracking.
 */
public abstract class BaseWorkflowRequest {
    protected String workflowId;
    protected String workflowVersion = "latest";
    protected String tenantId;
    protected String triggeredBy;
    protected String correlationId;
    protected Map<String, Object> inputs = new HashMap<>();
    protected Map<String, Object> metadata = new HashMap<>();
    protected ExecutionOptions executionOptions = new ExecutionOptions();
    protected Instant scheduledAt;

    // Getters and setters
    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowVersion() {
        return workflowVersion;
    }

    public void setWorkflowVersion(String workflowVersion) {
        this.workflowVersion = workflowVersion;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public ExecutionOptions getExecutionOptions() {
        return executionOptions;
    }

    public void setExecutionOptions(ExecutionOptions executionOptions) {
        this.executionOptions = executionOptions;
    }

    public Instant getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Instant scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
}
