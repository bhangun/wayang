package tech.kayys.wayang.memory.visual;

/**
 * Scoped query parameters for retrieving memory visualization data.
 */
public record MemoryVisualQuery(
        String agentId,
        String userId,
        String tenantId,
        String workspaceId,
        String sessionId,
        String category,
        int limit,
        int dimensions
) {
    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT = 1000;
    public static final int DEFAULT_DIMENSIONS = 3;

    public MemoryVisualQuery(String agentId, String userId, String tenantId, String workspaceId, String sessionId, String category, int limit) {
        this(agentId, userId, tenantId, workspaceId, sessionId, category, limit, DEFAULT_DIMENSIONS);
    }

    public MemoryVisualQuery {
        agentId = agentId == null ? "default" : agentId;
        tenantId = tenantId == null ? "default" : tenantId;
        workspaceId = workspaceId == null ? "default" : workspaceId;
        if (limit <= 0) limit = DEFAULT_LIMIT;
        if (limit > MAX_LIMIT) limit = MAX_LIMIT;
        if (dimensions != 2 && dimensions != 3) dimensions = DEFAULT_DIMENSIONS;
    }

    public static MemoryVisualQuery forAgent(String agentId) {
        return new MemoryVisualQuery(agentId, null, "default", "default", null, null, DEFAULT_LIMIT, DEFAULT_DIMENSIONS);
    }

    public static MemoryVisualQuery forSession(String sessionId) {
        return new MemoryVisualQuery("default", null, "default", "default", sessionId, null, DEFAULT_LIMIT, DEFAULT_DIMENSIONS);
    }

    public static MemoryVisualQuery forUser(String userId) {
        return new MemoryVisualQuery("default", userId, "default", "default", null, null, DEFAULT_LIMIT, DEFAULT_DIMENSIONS);
    }
}
