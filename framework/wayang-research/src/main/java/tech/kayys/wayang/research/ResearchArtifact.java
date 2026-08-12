package tech.kayys.wayang.research;

import tech.kayys.wayang.project.ProjectContext;

/**
 * An artifact produced by a research step (e.g. a downloaded source, extracted fact, or summary).
 */
public record ResearchArtifact(
    String id,
    ProjectContext project,
    String sessionId,
    String stepId,
    String contentType,
    String content
) {}
