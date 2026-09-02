package tech.kayys.wayang.memory.visual;

import java.util.Map;

/**
 * Visual insight badge or card derived from memory analysis.
 */
public record MemoryVisualInsight(
        String type,
        String description,
        double confidence,
        Map<String, Object> metadata
) {
    public MemoryVisualInsight {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
