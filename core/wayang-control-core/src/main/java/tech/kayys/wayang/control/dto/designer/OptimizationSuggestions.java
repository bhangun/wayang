package tech.kayys.wayang.control.dto.designer;

import java.util.List;

/**
 * Collection of optimization suggestions for a design.
 */
public record OptimizationSuggestions(
        String routeId,
        List<OptimizationSuggestion> suggestions) {
}
