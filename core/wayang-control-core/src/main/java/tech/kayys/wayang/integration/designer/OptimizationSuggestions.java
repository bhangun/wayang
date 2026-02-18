package tech.kayys.wayang.integration.designer;

import java.util.List;

public record OptimizationSuggestions(
        String routeId,
        List<OptimizationSuggestion> suggestions) {
}
