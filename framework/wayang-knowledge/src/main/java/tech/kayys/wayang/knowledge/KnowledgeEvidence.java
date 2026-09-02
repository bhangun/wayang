package tech.kayys.wayang.knowledge;

import java.util.Map;

/**
 * Evidence returned by a knowledge source.
 */
public record KnowledgeEvidence(
        KnowledgeItem item,
        double score,
        String retrievalMethod,
        Map<String, Object> metadata
) {

    public KnowledgeEvidence {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeEvidence of(KnowledgeItem item, double score, String retrievalMethod) {
        return new KnowledgeEvidence(item, score, retrievalMethod, Map.of());
    }
}
