package tech.kayys.wayang.knowledge.exchange.journal;

public record KnowledgeAnswerResolutionCommitIndex(
        long committedIndex,
        long appliedIndex,
        long lastIndex,
        long currentTerm
) {
    public KnowledgeAnswerResolutionCommitIndex {
        if (committedIndex < -1 || appliedIndex < -1 || lastIndex < -1 || currentTerm < 0) {
            throw new IllegalArgumentException("indexes must be >= -1 and term must be >= 0");
        }

        if (committedIndex > lastIndex && lastIndex != -1) {
            throw new IllegalArgumentException("committedIndex cannot exceed lastIndex");
        }

        if (appliedIndex > committedIndex && committedIndex != -1) {
            throw new IllegalArgumentException("appliedIndex cannot exceed committedIndex");
        }
    }

    public static KnowledgeAnswerResolutionCommitIndex initial() {
        return new KnowledgeAnswerResolutionCommitIndex(-1, -1, -1, 0);
    }
}
