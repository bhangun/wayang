package tech.kayys.wayang.knowledge.reasoning;

import tech.kayys.wayang.knowledge.KnowledgeProvenance;

import java.util.Map;

/**
 * Granular evidence piece used in a decision.
 */
public record Evidence(
        String id,
        String text,
        double relevance,
        String authority,
        KnowledgeProvenance provenance,
        Map<String, Object> metadata
) {

    public Evidence {
        text = text == null ? "" : text;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static Evidence of(String id, String text, double relevance, String authority, KnowledgeProvenance provenance) {
        return new Evidence(id, text, relevance, authority, provenance, Map.of());
    }
}
