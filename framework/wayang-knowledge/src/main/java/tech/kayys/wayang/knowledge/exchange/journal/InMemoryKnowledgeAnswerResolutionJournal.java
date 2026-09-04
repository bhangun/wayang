package tech.kayys.wayang.knowledge.exchange.journal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

public final class InMemoryKnowledgeAnswerResolutionJournal
        implements KnowledgeAnswerResolutionJournal {

    private final ConcurrentSkipListMap<Long, KnowledgeAnswerResolutionLogEntry> entries =
            new ConcurrentSkipListMap<>();

    @Override
    public synchronized KnowledgeAnswerResolutionLogEntry append(
            KnowledgeAnswerResolutionLogEntry entry) {

        long expected = entries.isEmpty() ? 0 : entries.lastKey() + 1;

        if (entry.index() != expected) {
            throw new IllegalStateException(
                    "Journal index gap: expected " + expected + " but got " + entry.index());
        }

        entries.put(entry.index(), entry);
        return entry;
    }

    @Override
    public Optional<KnowledgeAnswerResolutionLogEntry> get(long index) {
        return Optional.ofNullable(entries.get(index));
    }

    @Override
    public List<KnowledgeAnswerResolutionLogEntry> range(long fromInclusive, long toInclusive) {
        if (fromInclusive > toInclusive || entries.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(entries.subMap(fromInclusive, true, toInclusive, true).values());
    }

    @Override
    public long lastIndex() {
        return entries.isEmpty() ? -1 : entries.lastKey();
    }

    @Override
    public synchronized void truncateFrom(long index) {
        entries.tailMap(index, true).clear();
    }
}
