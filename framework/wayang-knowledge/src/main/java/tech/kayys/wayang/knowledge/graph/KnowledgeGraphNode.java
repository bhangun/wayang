package tech.kayys.wayang.knowledge.graph;

import java.util.Map;

/**
 * Normalized UI-facing graph node for visualization.
 * Decouples the UI from internal domain graph node types.
 */
public record KnowledgeGraphNode(
        String id,
        String type,
        String label,
        double weight,
        String tenantId,
        String workspaceId,
        String projectId,
        Map<String, Object> metadata
) {
    public KnowledgeGraphNode {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        type = type == null ? "UNKNOWN" : type;
        label = label == null ? id : label;
    }

    public static KnowledgeGraphNode of(String id, String type, String label, double weight) {
        return new KnowledgeGraphNode(id, type, label, weight, null, null, null, Map.of());
    }

    public static KnowledgeGraphNode of(String id, String type, String label, double weight,
                                        String tenantId, String workspaceId, String projectId) {
        return new KnowledgeGraphNode(id, type, label, weight, tenantId, workspaceId, projectId, Map.of());
    }
}
