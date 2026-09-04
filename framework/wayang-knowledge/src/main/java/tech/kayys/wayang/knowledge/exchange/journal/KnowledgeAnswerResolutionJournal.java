package tech.kayys.wayang.knowledge.exchange.journal;

import java.util.List;
import java.util.Optional;

public interface KnowledgeAnswerResolutionJournal {

    KnowledgeAnswerResolutionLogEntry append(KnowledgeAnswerResolutionLogEntry entry);

    Optional<KnowledgeAnswerResolutionLogEntry> get(long index);

    List<KnowledgeAnswerResolutionLogEntry> range(long fromInclusive, long toInclusive);

    long lastIndex();

    void truncateFrom(long index);
}
