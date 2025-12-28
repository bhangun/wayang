package tech.kayys.wayang.schema.execution;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.Arrays;
import java.util.List;

/**
 * HumanReviewConfig - Human review configuration for error handling
 */
@RegisterForReflection
public class HumanReviewConfig {
    private Boolean enabled = false;
    private String thresholdSeverity = "CRITICAL";
    private String reviewQueue;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getThresholdSeverity() {
        return thresholdSeverity;
    }

    public void setThresholdSeverity(String thresholdSeverity) {
        List<String> validSeverities = Arrays.asList("NEVER", "INFO", "WARNING", "ERROR", "CRITICAL");
        if (!validSeverities.contains(thresholdSeverity)) {
            throw new IllegalArgumentException("Invalid threshold severity: " + thresholdSeverity);
        }
        this.thresholdSeverity = thresholdSeverity;
    }

    public String getReviewQueue() {
        return reviewQueue;
    }

    public void setReviewQueue(String reviewQueue) {
        this.reviewQueue = reviewQueue;
    }
}
