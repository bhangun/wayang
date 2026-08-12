package tech.kayys.wayang.project;

/**
 * Defines the ownership hierarchy and scope for an operation or artifact within a project.
 */
public record ProjectScope(
    String tenantId,
    String projectId,
    String conversationId,
    String executionId
) {}
