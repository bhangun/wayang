package tech.kayys.wayang.schema.execution;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;
import java.util.List;

@RegisterForReflection
public class ErrorHandlingConfig {
    private RetryPolicy retryPolicy;
    private FallbackConfig fallback;
    private String humanReviewThreshold = "CRITICAL";
    private Boolean autoHealEnabled = false;
    private CircuitBreaker circuitBreaker;
    private EscalationConfig escalation;
    private HumanReviewConfig humanReview;

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    public FallbackConfig getFallback() {
        return fallback;
    }

    public void setFallback(FallbackConfig fallback) {
        this.fallback = fallback;
    }

    public String getHumanReviewThreshold() {
        return humanReviewThreshold;
    }

    public void setHumanReviewThreshold(String humanReviewThreshold) {
        List<String> validThresholds = Arrays.asList("NEVER", "INFO", "WARNING", "ERROR", "CRITICAL");
        if (!validThresholds.contains(humanReviewThreshold)) {
            throw new IllegalArgumentException("Invalid human review threshold: " + humanReviewThreshold);
        }
        this.humanReviewThreshold = humanReviewThreshold;
    }

    public Boolean getAutoHealEnabled() {
        return autoHealEnabled;
    }

    public void setAutoHealEnabled(Boolean autoHealEnabled) {
        this.autoHealEnabled = autoHealEnabled;
    }

    public CircuitBreaker getCircuitBreaker() {
        return circuitBreaker;
    }

    public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        this.circuitBreaker = circuitBreaker;
    }

    public EscalationConfig getEscalation() {
        return escalation;
    }

    public void setEscalation(EscalationConfig escalation) {
        this.escalation = escalation;
    }

    public HumanReviewConfig getHumanReview() {
        return humanReview;
    }

    public void setHumanReview(HumanReviewConfig humanReview) {
        this.humanReview = humanReview;
    }

    // Deprecated field kept for backward compatibility
    @Deprecated
    public String getFallbackNodeId() {
        return fallback != null ? fallback.getNodeId() : null;
    }

    @Deprecated
    public void setFallbackNodeId(String fallbackNodeId) {
        if (fallback == null) {
            fallback = new FallbackConfig();
        }
        fallback.setNodeId(fallbackNodeId);
        fallback.setType("node");
    }
}
