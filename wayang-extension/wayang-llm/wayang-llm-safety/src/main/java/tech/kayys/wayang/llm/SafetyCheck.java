package tech.kayys.wayang.models.safety;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Result of a safety check.
 */
@Value
@Builder
public class SafetyCheck {
    boolean safe;
    double confidenceScore;
    List<Violation> violations;
    String sanitizedContent;
    
    @Value
    @Builder
    public static class Violation {
        String type;
        String severity;
        String description;
        int startIndex;
        int endIndex;
    }
    
    public static SafetyCheck safe() {
        return SafetyCheck.builder()
            .safe(true)
            .confidenceScore(1.0)
            .violations(List.of())
            .build();
    }
    
    public static SafetyCheck unsafe(List<Violation> violations) {
        return SafetyCheck.builder()
            .safe(false)
            .confidenceScore(0.0)
            .violations(violations)
            .build();
    }
}