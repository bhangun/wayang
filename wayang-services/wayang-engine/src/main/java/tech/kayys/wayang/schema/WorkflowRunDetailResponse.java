package tech.kayys.wayang.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import tech.kayys.wayang.engine.NodeExecutionRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow Run Detail Response.
 * 
 * Provides detailed information about a workflow run.
 * 
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowRunDetailResponse {

    private String runId;
    private String workflowId;
    private String workflowName;
    private String status;
    private String startTime;
    private String endTime;
    private Long duration;
    private Map<String, Object> inputs;
    private Map<String, Object> output;
    private List<NodeExecutionRecord> nodeExecutions;
    private List<ErrorInfo> errors;
    private Map<String, Object> metadata;
    private String triggeredBy;
    private String tenantId;

    public WorkflowRunDetailResponse() {
        this.inputs = new HashMap<>();
        this.output = new HashMap<>();
        this.nodeExecutions = new ArrayList<>();
        this.errors = new ArrayList<>();
        this.metadata = new HashMap<>();
    }

    /**
     * Error information.
     */
    public static class ErrorInfo {
        private String nodeId;
        private String errorType;
        private String message;
        private String timestamp;
        private Map<String, Object> details;

        public ErrorInfo() {
            this.details = new HashMap<>();
        }

        // Getters and setters
        public String getNodeId() {
            return nodeId;
        }

        public void setNodeId(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public void setDetails(Map<String, Object> details) {
            this.details = details;
        }
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

    public Map<String, Object> getInputs() {
        return inputs;
    }

    public void setInputs(Map<String, Object> inputs) {
        this.inputs = inputs;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public List<NodeExecutionRecord> getNodeExecutions() {
        return nodeExecutions;
    }

    public void setNodeExecutions(List<NodeExecutionRecord> nodeExecutions) {
        this.nodeExecutions = nodeExecutions;
    }

    public List<ErrorInfo> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorInfo> errors) {
        this.errors = errors;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
