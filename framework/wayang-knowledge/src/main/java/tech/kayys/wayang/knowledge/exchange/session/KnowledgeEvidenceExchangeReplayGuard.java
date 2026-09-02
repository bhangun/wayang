package tech.kayys.wayang.knowledge.exchange.session;

import java.time.Instant;

public interface KnowledgeEvidenceExchangeReplayGuard {

    KnowledgeEvidenceExchangeReplayStatus checkAndRecord(
            KnowledgeEvidenceExchangeRequestBinding binding,
            String principalId,
            Instant now
    );

    void removeExpired(Instant now);
}
