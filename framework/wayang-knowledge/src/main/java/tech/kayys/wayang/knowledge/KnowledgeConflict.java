package tech.kayys.wayang.knowledge;

/**
 * Describes a contradiction or disagreement between two knowledge evidence items.
 */
public record KnowledgeConflict(
        KnowledgeEvidence left,
        KnowledgeEvidence right,
        ConflictKind kind,
        String description
) {

    public enum ConflictKind {
        DIRECT_CONTRADICTION,
        TEMPORAL_OVERLAP,
        AUTHORITY_DISAGREEMENT,
        JURISDICTION_CONFLICT,
        SUPERSEDED_UNRESOLVED
    }

    public KnowledgeConflict {
        kind = kind == null ? ConflictKind.DIRECT_CONTRADICTION : kind;
        description = description == null ? "" : description;
    }

    public static KnowledgeConflict contradiction(KnowledgeEvidence left, KnowledgeEvidence right, String description) {
        return new KnowledgeConflict(left, right, ConflictKind.DIRECT_CONTRADICTION, description);
    }
}
