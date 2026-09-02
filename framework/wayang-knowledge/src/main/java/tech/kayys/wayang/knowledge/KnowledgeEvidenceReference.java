package tech.kayys.wayang.knowledge;

import java.util.Map;

/**
 * A concrete piece of evidence used during reasoning.
 */
public record KnowledgeEvidenceReference(

        String knowledgeId,

        String versionId,

        String fragmentId,

        String excerpt,

        double relevance,

        double authority,

        double trust,

        Map<String, Object> metadata
) {

    public KnowledgeEvidenceReference {
        metadata = metadata == null
                ? Map.of()
                : Map.copyOf(metadata);

        relevance = clamp(relevance);
        authority = clamp(authority);
        trust = clamp(trust);
    }

    private static double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
