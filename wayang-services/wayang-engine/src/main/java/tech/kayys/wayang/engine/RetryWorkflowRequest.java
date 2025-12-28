package tech.kayys.wayang.engine;

import java.util.Map;

/**
 * 
 * Retry workflow request.
 */
public class RetryWorkflowRequest {
    private boolean fromCheckpoint = true;
    private String startFromNodeId;
    private Map<String, Object> overrideInputs;
    private Integer maxAttempts = 3;

    // Getters and setters
    public boolean isFromCheckpoint() {
        return fromCheckpoint;
    }

    public void setFromCheckpoint(boolean fromCheckpoint) {
        this.fromCheckpoint = fromCheckpoint;
    }

    public String getStartFromNodeId() {
        return startFromNodeId;
    }

    public void setStartFromNodeId(String startFromNodeId) {
        this.startFromNodeId = startFromNodeId;
    }

    public Map<String, Object> getOverrideInputs() {
        return overrideInputs;
    }

    public void setOverrideInputs(Map<String, Object> overrideInputs) {
        this.overrideInputs = overrideInputs;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }
}
