package tech.kayys.wayang.knowledge.exchange.journal;

public interface KnowledgeAnswerResolutionCommitService {

    KnowledgeAnswerResolutionCommitIndex commitThrough(long index, long term);

    KnowledgeAnswerResolutionCommitIndex applyThrough(long index);

    KnowledgeAnswerResolutionCommitIndex currentIndex();
}
