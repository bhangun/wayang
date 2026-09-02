package tech.kayys.wayang.knowledge.reasoning;

import java.util.List;
import java.util.Map;

/**
 * Comprehensive result of a reasoning evaluation.
 */
public record Decision(
        String conclusion,
        List<Evidence> evidence,
        List<String> applicablePolicies,
        List<String> conflicts,
        List<String> assumptions,
        double confidence,
        Map<String, Object> metadata
) {

    public Decision {
        conclusion = conclusion == null ? "" : conclusion;
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        applicablePolicies = applicablePolicies == null ? List.of() : List.copyOf(applicablePolicies);
        conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
        assumptions = assumptions == null ? List.of() : List.copyOf(assumptions);
        confidence = Math.max(0.0, Math.min(1.0, confidence));
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
