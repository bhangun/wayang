package tech.kayys.wayang.knowledge.lineage;

import java.time.Instant;
import java.util.Map;

/**
 * Directed edge in a knowledge lineage graph.
 */
public record KnowledgeLineageEdge(
        String id,
        String sourceId,
        String targetId,
        LineageRelation relation,
        double confidence,
        Instant establishedAt,
        Map<String, Object> metadata
) {

    public enum LineageRelation {
        DERIVED_FROM,
        SUPERSEDES,
        CITES,
        CONTRADICTS,
        SUPPORTS,
        INFLUENCED_BY
    }

    public KnowledgeLineageEdge {
        id = id == null ? "edge-" + java.util.UUID.randomUUID() : id;
        relation = relation == null ? LineageRelation.DERIVED_FROM : relation;
        establishedAt = establishedAt == null ? Instant.now() : establishedAt;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static KnowledgeLineageEdge derivation(String sourceId, String targetId, double confidence) {
        return new KnowledgeLineageEdge(null, sourceId, targetId, LineageRelation.DERIVED_FROM, confidence, Instant.now(), Map.of());
    }

    public static KnowledgeLineageEdge supersession(String sourceId, String targetId) {
        return new KnowledgeLineageEdge(null, sourceId, targetId, LineageRelation.SUPERSEDES, 1.0, Instant.now(), Map.of());
    }
}
