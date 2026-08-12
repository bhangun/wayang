package tech.kayys.wayang.knowledge;

import java.time.Instant;
import java.util.List;
import tech.kayys.wayang.project.ProjectContext;

/**
 * Represents a curated fact, decision, or finding stored as project knowledge.
 */
public record KnowledgeItem(
    String id,
    ProjectContext project,
    String content,
    KnowledgeType type,
    List<Provenance> provenance,
    double confidence,
    double importance,
    String supersededBy,
    Instant createdAt,
    Instant updatedAt
) {}
