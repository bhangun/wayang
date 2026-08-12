package tech.kayys.wayang.project;

/**
 * Represents the root isolation boundary for a project in Wayang.
 * Every persistent operation must carry this context to ensure isolation.
 */
public record ProjectContext(
    String projectId,
    String tenantId,
    String workspaceId
) {}
