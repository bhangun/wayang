package tech.kayys.wayang.security.obligation;

import java.util.List;
import java.util.Map;

/**
 * Aggregated outcome of executing a sequence of obligations through the pipeline.
 */
public record ObligationPipelineResult(
        boolean successful,
        boolean continueExecution,
        List<ObligationResult> results,
        Map<String, Object> attributes
) {

    public ObligationPipelineResult {
        results = results == null ? List.of() : List.copyOf(results);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static ObligationPipelineResult success(List<ObligationResult> results) {
        return new ObligationPipelineResult(true, true, results, Map.of());
    }

    public static ObligationPipelineResult failure(String error, List<ObligationResult> results) {
        return new ObligationPipelineResult(false, false, results, Map.of("error", error));
    }
}
