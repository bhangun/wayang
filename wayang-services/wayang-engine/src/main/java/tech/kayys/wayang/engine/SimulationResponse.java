package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Simulation response.
 */
public class SimulationResponse {
    private String simulationId;
    private String workflowId;
    private boolean validationPassed;
    private List<PlannedStep> predictedPath;
    private Map<String, Object> predictedOutput;
    private List<String> warnings;
    private Long estimatedDurationMs;
    private Instant simulatedAt;

    // Getters and setters
    public String getSimulationId() {
        return simulationId;
    }

    public void setSimulationId(String simulationId) {
        this.simulationId = simulationId;
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    public boolean isValidationPassed() {
        return validationPassed;
    }

    public void setValidationPassed(boolean validationPassed) {
        this.validationPassed = validationPassed;
    }

    public List<PlannedStep> getPredictedPath() {
        return predictedPath;
    }

    public void setPredictedPath(List<PlannedStep> predictedPath) {
        this.predictedPath = predictedPath;
    }

    public Map<String, Object> getPredictedOutput() {
        return predictedOutput;
    }

    public void setPredictedOutput(Map<String, Object> predictedOutput) {
        this.predictedOutput = predictedOutput;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public Long getEstimatedDurationMs() {
        return estimatedDurationMs;
    }

    public void setEstimatedDurationMs(Long estimatedDurationMs) {
        this.estimatedDurationMs = estimatedDurationMs;
    }

    public Instant getSimulatedAt() {
        return simulatedAt;
    }

    public void setSimulatedAt(Instant simulatedAt) {
        this.simulatedAt = simulatedAt;
    }
}
