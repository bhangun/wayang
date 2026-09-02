package tech.kayys.wayang.knowledge.graph;

import java.util.Map;

/**
 * Normalized UI-facing directed edge between two knowledge graph nodes.
 * Decouples the UI from internal domain edge types (lineage, provenance, artifact relation, etc.).
 */
public record KnowledgeGraphEdge(
        String id,
        String source,
        String target,
        String relation,
        double confidence,
        Map<String, Object> metadata
) {
    public KnowledgeGraphEdge {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        relation = relation == null ? "RELATED_TO" : relation;
        id = id == null ? "edge-" + java.util.UUID.randomUUID() : id;
    }

    public static KnowledgeGraphEdge of(String source, String target, String relation, double confidence) {
        return new KnowledgeGraphEdge(null, source, target, relation, confidence, Map.of());
    }
}
