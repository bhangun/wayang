package tech.kayys.wayang.memory.visual;

import java.util.Map;

/**
 * Normalized UI-facing directed link between memory nodes.
 */
public record MemoryVisualEdge(
        String id,
        String source,
        String target,
        String relation,
        double confidence,
        Map<String, Object> metadata
) {
    public MemoryVisualEdge {
        id = id == null ? "medge-" + java.util.UUID.randomUUID() : id;
        relation = relation == null ? "RELATES_TO" : relation;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static MemoryVisualEdge of(String source, String target, String relation, double confidence) {
        return new MemoryVisualEdge(null, source, target, relation, confidence, Map.of());
    }
}
