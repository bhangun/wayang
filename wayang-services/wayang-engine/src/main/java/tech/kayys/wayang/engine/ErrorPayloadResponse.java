package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.Map;

/**
 * Error payload response matching the schema.
 */
public class ErrorPayloadResponse {
    private String type; // ToolError, LLMError, NetworkError, ValidationError, etc.
    private String message;
    private Map<String, Object> details;
    private Boolean retryable;
    private String originNode;
    private String originRunId;
    private Integer attempt;
    private Integer maxAttempts;
    private Instant timestamp;
    private String suggestedAction; // retry, fallback, escalate, human_review, abort, auto_fix
    private String provenanceRef;
    private String stackTrace;

    // Getters and setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public Boolean getRetryable() {
        return retryable;
    }

    public void setRetryable(Boolean retryable) {
        this.retryable = retryable;
    }

    public String getOriginNode() {
        return originNode;
    }

    public void setOriginNode(String originNode) {
        this.originNode = originNode;
    }

    public String getOriginRunId() {
        return originRunId;
    }

    public void setOriginRunId(String originRunId) {
        this.originRunId = originRunId;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getSuggestedAction() {
        return suggestedAction;
    }

    public void setSuggestedAction(String suggestedAction) {
        this.suggestedAction = suggestedAction;
    }

    public String getProvenanceRef() {
        return provenanceRef;
    }

    public void setProvenanceRef(String provenanceRef) {
        this.provenanceRef = provenanceRef;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }
}
