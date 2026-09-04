package tech.kayys.wayang.knowledge.exchange.journal;

public final class DefaultKnowledgeAnswerResolutionCommitService
        implements KnowledgeAnswerResolutionCommitService {

    private long committedIndex = -1;
    private long appliedIndex = -1;
    private long currentTerm = 0;

    @Override
    public synchronized KnowledgeAnswerResolutionCommitIndex commitThrough(long index, long term) {
        if (index < committedIndex) {
            throw new IllegalArgumentException("Commit index cannot move backwards: current=" + committedIndex + ", target=" + index);
        }
        if (term < currentTerm) {
            throw new IllegalArgumentException("Term cannot move backwards");
        }

        this.committedIndex = index;
        this.currentTerm = term;
        return currentIndex();
    }

    @Override
    public synchronized KnowledgeAnswerResolutionCommitIndex applyThrough(long index) {
        if (index < appliedIndex) {
            throw new IllegalArgumentException("Applied index cannot move backwards");
        }
        if (index > committedIndex && committedIndex != -1) {
            throw new IllegalArgumentException("Cannot apply uncommitted index: applied=" + index + ", committed=" + committedIndex);
        }

        this.appliedIndex = index;
        return currentIndex();
    }

    @Override
    public synchronized KnowledgeAnswerResolutionCommitIndex currentIndex() {
        return new KnowledgeAnswerResolutionCommitIndex(
                committedIndex,
                appliedIndex,
                Math.max(committedIndex, appliedIndex),
                currentTerm
        );
    }
}
