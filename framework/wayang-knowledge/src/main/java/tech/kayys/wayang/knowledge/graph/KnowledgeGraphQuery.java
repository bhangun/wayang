package tech.kayys.wayang.knowledge.graph;

/**
 * Query parameters for filtering a knowledge graph by scope.
 * Used by both REST API request bodies and service layer calls.
 */
public record KnowledgeGraphQuery(
        String entityId,
        String tenantId,
        String workspaceId,
        String projectId,
        String sessionId,
        int maxDepth,
        int dimensions
) {
    public static final int DEFAULT_MAX_DEPTH = 5;
    public static final int MAX_ALLOWED_DEPTH = 10;
    public static final int DEFAULT_DIMENSIONS = 3;

    public KnowledgeGraphQuery(String entityId, String tenantId, String workspaceId,
                               String projectId, String sessionId, int maxDepth) {
        this(entityId, tenantId, workspaceId, projectId, sessionId, maxDepth, DEFAULT_DIMENSIONS);
    }

    public KnowledgeGraphQuery {
        if (maxDepth <= 0) maxDepth = DEFAULT_MAX_DEPTH;
        if (maxDepth > MAX_ALLOWED_DEPTH) maxDepth = MAX_ALLOWED_DEPTH;
        tenantId = tenantId == null ? "default" : tenantId;
        workspaceId = workspaceId == null ? "default" : workspaceId;
        if (dimensions != 2 && dimensions != 3) dimensions = DEFAULT_DIMENSIONS;
    }

    public static KnowledgeGraphQuery of(String entityId, String tenantId, String workspaceId) {
        return new KnowledgeGraphQuery(entityId, tenantId, workspaceId, null, null, DEFAULT_MAX_DEPTH, DEFAULT_DIMENSIONS);
    }

    public static KnowledgeGraphQuery forLineage(String nodeId, int maxDepth) {
        return new KnowledgeGraphQuery(nodeId, "default", "default", null, null, maxDepth, DEFAULT_DIMENSIONS);
    }
}
