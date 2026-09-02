package tech.kayys.wayang.knowledge.lineage;

import java.util.Map;

/**
 * Direct reference from a conclusion or claim back to an evidence item.
 */
public record EvidenceReference(
        String evidenceId,
        String itemId,
        double weight,
        String contribution,
        Map<String, Object> metadata
) {

    public EvidenceReference {
        contribution = contribution == null ? "" : contribution;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static EvidenceReference of(String evidenceId, String itemId, double weight, String contribution) {
        return new EvidenceReference(evidenceId, itemId, weight, contribution, Map.of());
    }
}
