package tech.kayys.wayang.knowledge.exchange.session;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeEvidenceExchangeSessionStore
        implements KnowledgeEvidenceExchangeSessionStore {

    private final Map<String, KnowledgeEvidenceExchangeSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void create(KnowledgeEvidenceExchangeSession session) {
        KnowledgeEvidenceExchangeSession previous = sessions.putIfAbsent(session.sessionId(), session);
        if (previous != null) {
            throw new IllegalStateException("Exchange session already exists: " + session.sessionId());
        }
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeSession> get(String sessionId) {
        if (sessionId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(sessions.get(sessionId));
    }

    @Override
    public void update(KnowledgeEvidenceExchangeSession session) {
        sessions.computeIfPresent(session.sessionId(), (k, v) -> session);
    }

    @Override
    public void removeExpired(Instant now) {
        sessions.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }
}
