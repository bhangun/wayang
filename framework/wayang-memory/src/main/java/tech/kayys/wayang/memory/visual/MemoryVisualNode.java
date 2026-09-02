package tech.kayys.wayang.memory.visual;

import java.util.Map;

/**
 * Normalized UI-facing graph node representing a memory concept, fact, entity, or task pattern.
 */
public record MemoryVisualNode(
        String id,
        String type,
        String label,
        double weight,
        String cluster,
        String tier,
        Map<String, Object> metadata
) {
    public MemoryVisualNode {
        type = type == null ? "CONCEPT" : type;
        label = label == null ? id : label;
        cluster = cluster == null ? "default" : cluster;
        tier = tier == null ? "SEMANTIC" : tier;
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static MemoryVisualNode of(String id, String type, String label, double weight) {
        return new MemoryVisualNode(id, type, label, weight, "default", "SEMANTIC", Map.of());
    }

    public static MemoryVisualNode of(String id, String type, String label, double weight, String cluster, String tier) {
        return new MemoryVisualNode(id, type, label, weight, cluster, tier, Map.of());
    }
}
