package tech.kayys.wayang.engine;

import java.time.Instant;
import java.util.List;

/**
 * Execution plan response for orchestrated workflows.
 */
public class ExecutionPlanResponse {
    private String runId;
    private String planId;
    private String strategy; // sequential, parallel, conditional, dynamic
    private List<PlannedStep> steps;
    private String decisionReasoning;
    private Double confidence;
    private Instant createdAt;

    // Getters and setters
    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public List<PlannedStep> getSteps() {
        return steps;
    }

    public void setSteps(List<PlannedStep> steps) {
        this.steps = steps;
    }

    public String getDecisionReasoning() {
        return decisionReasoning;
    }

    public void setDecisionReasoning(String reasoning) {
        this.decisionReasoning = reasoning;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
