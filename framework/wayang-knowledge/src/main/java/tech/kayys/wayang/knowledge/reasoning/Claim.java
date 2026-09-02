package tech.kayys.wayang.knowledge.reasoning;

import java.util.List;
import java.util.Map;

/**
 * An assertion or claim supported by evidence.
 */
public record Claim(
        String id,
        String statement,
        double confidence,
        List<String> supportingEvidenceIds,
        Map<String, Object> metadata
) {

    public Claim {
        statement = statement == null ? "" : statement;
        supportingEvidenceIds = supportingEvidenceIds == null ? List.of() : List.copyOf(supportingEvidenceIds);
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static Claim of(String id, String statement, double confidence, List<String> evidenceIds) {
        return new Claim(id, statement, confidence, evidenceIds, Map.of());
    }
}
