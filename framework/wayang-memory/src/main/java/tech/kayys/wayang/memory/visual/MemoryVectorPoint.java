package tech.kayys.wayang.memory.visual;

import java.util.Map;

/**
 * Normalized 2D/3D coordinate representing a projected vector memory embedding.
 * Used by UI scatter plots (Plotly, Deck.gl, Three.js) to show semantic memory clusters.
 */
public record MemoryVectorPoint(
        String id,
        double x,
        double y,
        double z,
        String category,
        String label,
        double relevance,
        Map<String, Object> metadata
) {
    public MemoryVectorPoint {
        category = category == null ? "General" : category;
        label = label == null ? id : label;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static MemoryVectorPoint point2D(String id, double x, double y, String category, String label, double relevance) {
        return new MemoryVectorPoint(id, x, y, 0.0, category, label, relevance, Map.of());
    }

    public static MemoryVectorPoint point3D(String id, double x, double y, double z, String category, String label, double relevance) {
        return new MemoryVectorPoint(id, x, y, z, category, label, relevance, Map.of());
    }
}
