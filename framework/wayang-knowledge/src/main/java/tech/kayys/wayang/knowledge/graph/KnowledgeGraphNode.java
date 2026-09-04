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
        double x,
        double y,
        double z,
        String tenantId,
        String workspaceId,
        String projectId,
        Map<String, Object> metadata
) {
    public KnowledgeGraphNode(String id, String type, String label, double weight,
                              String tenantId, String workspaceId, String projectId,
                              Map<String, Object> metadata) {
        this(id, type, label, weight, 0.0, 0.0, 0.0, tenantId, workspaceId, projectId, metadata);
    }

    public KnowledgeGraphNode {
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
        type = type == null ? "UNKNOWN" : type;
        label = label == null ? id : label;
    }

    public static KnowledgeGraphNode of(String id, String type, String label, double weight) {
        return new KnowledgeGraphNode(id, type, label, weight, 0.0, 0.0, 0.0, null, null, null, Map.of());
    }

    public static KnowledgeGraphNode of(String id, String type, String label, double weight,
                                        String tenantId, String workspaceId, String projectId) {
        return new KnowledgeGraphNode(id, type, label, weight, 0.0, 0.0, 0.0, tenantId, workspaceId, projectId, Map.of());
    }

    public static KnowledgeGraphNode of3D(String id, String type, String label, double weight,
                                          double x, double y, double z,
                                          String tenantId, String workspaceId, String projectId) {
        return new KnowledgeGraphNode(id, type, label, weight, x, y, z, tenantId, workspaceId, projectId, Map.of());
    }
}
