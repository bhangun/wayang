package tech.kayys.wayang.control.dto.designer;

/**
 * A specific optimization suggestion for a route design.
 */
public record OptimizationSuggestion(
        String nodeId,
        String suggestion,
        String impact, // e.g. "performance", "cost", "reliability"
        String confidence) {
}
