package tech.kayys.wayang.knowledge.exchange.journal;

public record KnowledgeAnswerResolutionJournalState(
        long lastIndex,
        long committedIndex,
        long appliedIndex,
        long currentTerm,
        String currentEpochId
) {
    public KnowledgeAnswerResolutionJournalState {
        if (lastIndex < -1 || committedIndex < -1 || appliedIndex < -1 || currentTerm < 0) {
            throw new IllegalArgumentException("invalid journal state");
        }

        if (committedIndex > lastIndex && lastIndex != -1) {
            throw new IllegalArgumentException("commit index exceeds last index");
        }

        if (appliedIndex > committedIndex && committedIndex != -1) {
            throw new IllegalArgumentException("applied index exceeds commit index");
        }
    }
}
