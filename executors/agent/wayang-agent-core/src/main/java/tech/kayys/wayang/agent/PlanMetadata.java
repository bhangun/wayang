package tech.kayys.wayang.agent;

import java.util.Map;

/**
 * Plan Metadata - Additional info about the plan.
 */
public record PlanMetadata(
        String strategy,
        double confidenceScore,
        Map<String, Object> resourceUsage,
        String estimatedDuration) {
}
