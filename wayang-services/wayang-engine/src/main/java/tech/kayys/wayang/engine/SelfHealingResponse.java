package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.Map;

/**
 * Self-healing response.
 */
public class SelfHealingResponse {
    private String runId;
    private String nodeId;
    private boolean success;
    private Map<String, Object> correctedInput;
    private String reasoning;
    private Instant timestamp;

    // Getters and setters
    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Map<String, Object> getCorrectedInput() {
        return correctedInput;
    }

    public void setCorrectedInput(Map<String, Object> correctedInput) {
        this.correctedInput = correctedInput;
    }

    public String getReasoning() {
        return reasoning;
    }

    public void setReasoning(String reasoning) {
        this.reasoning = reasoning;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
