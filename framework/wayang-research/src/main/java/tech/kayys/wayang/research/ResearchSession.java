package tech.kayys.wayang.research;

import java.time.Instant;
import tech.kayys.wayang.project.ProjectContext;

/**
 * Represents a long-running, resumable research session.
 */
public record ResearchSession(
    String id,
    ProjectContext project,
    String question,
    ResearchStatus status,
    Instant startedAt,
    Instant completedAt
) {}
