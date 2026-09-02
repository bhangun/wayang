package tech.kayys.wayang.knowledge.exchange.session;

import java.time.Instant;
import java.util.Optional;

public interface KnowledgeEvidenceExchangeSessionStore {

    void create(KnowledgeEvidenceExchangeSession session);

    Optional<KnowledgeEvidenceExchangeSession> get(String sessionId);

    void update(KnowledgeEvidenceExchangeSession session);

    void removeExpired(Instant now);
}
