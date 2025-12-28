package tech.kayys.wayang.engine;

import java.util.Map;

/**
 * Planned execution step.
 */
public class PlannedStep {
    private Integer stepNumber;
    private String targetNodeId;
    private String targetType; // agent, connector, plugin
    private String condition;
    private Map<String, Object> parameters;
    private Integer estimatedDurationMs;

    // Getters and setters
    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getTargetNodeId() {
        return targetNodeId;
    }

    public void setTargetNodeId(String targetNodeId) {
        this.targetNodeId = targetNodeId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Integer getEstimatedDurationMs() {
        return estimatedDurationMs;
    }

    public void setEstimatedDurationMs(Integer durationMs) {
        this.estimatedDurationMs = durationMs;
    }
}
