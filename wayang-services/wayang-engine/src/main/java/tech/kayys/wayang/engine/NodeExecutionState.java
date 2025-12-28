package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.Map;

/**
 * Node execution state within a workflow.
 */
public class NodeExecutionState {
    private String nodeId;
    private String status; // PENDING, RUNNING, SUCCEEDED, FAILED, SKIPPED, RETRYING
    private Integer attempt;
    private Instant startedAt;
    private Instant completedAt;
    private Map<String, Object> input;
    private Map<String, Object> output;
    private ErrorPayloadResponse error;
    private Long durationMs;

    // Getters and setters
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getAttempt() {
        return attempt;
    }

    public void setAttempt(Integer attempt) {
        this.attempt = attempt;
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

    public Map<String, Object> getInput() {
        return input;
    }

    public void setInput(Map<String, Object> input) {
        this.input = input;
    }

    public Map<String, Object> getOutput() {
        return output;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    public ErrorPayloadResponse getError() {
        return error;
    }

    public void setError(ErrorPayloadResponse error) {
        this.error = error;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}
