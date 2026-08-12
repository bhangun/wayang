package tech.kayys.wayang.research;

import java.time.Instant;

/**
 * A discrete step executed within a research session.
 */
public record ResearchStep(
    String id,
    String sessionId,
    String action,
    String query,
    ResearchStatus status,
    Instant startedAt,
    Instant completedAt
) {}
