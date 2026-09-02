package tech.kayys.wayang.knowledge.exchange.session;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeEvidenceExchangeReplayGuard
        implements KnowledgeEvidenceExchangeReplayGuard {

    private final Map<String, KnowledgeEvidenceExchangeReplayRecord> records =
            new ConcurrentHashMap<>();

    @Override
    public KnowledgeEvidenceExchangeReplayStatus checkAndRecord(
            KnowledgeEvidenceExchangeRequestBinding binding,
            String principalId,
            Instant now
    ) {
        if (binding.isExpired(now)) {
            return KnowledgeEvidenceExchangeReplayStatus.EXPIRED;
        }

        String key = replayKey(binding);
        KnowledgeEvidenceExchangeReplayRecord existing = records.get(key);

        if (existing != null) {
            if (!existing.bindingFingerprint().equals(binding.bindingFingerprint())) {
                return KnowledgeEvidenceExchangeReplayStatus.INVALID_BINDING;
            }
            return KnowledgeEvidenceExchangeReplayStatus.REPLAYED;
        }

        KnowledgeEvidenceExchangeReplayRecord record = new KnowledgeEvidenceExchangeReplayRecord(
                binding.requestId(),
                binding.sessionId(),
                binding.nonce(),
                binding.bindingFingerprint(),
                principalId,
                binding.tenantId(),
                binding.runtimeId(),
                now,
                binding.expiresAt(),
                binding.metadata()
        );

        KnowledgeEvidenceExchangeReplayRecord previous = records.putIfAbsent(key, record);
        if (previous != null) {
            return KnowledgeEvidenceExchangeReplayStatus.REPLAYED;
        }

        return KnowledgeEvidenceExchangeReplayStatus.ACCEPTED;
    }

    @Override
    public void removeExpired(Instant now) {
        records.entrySet().removeIf(entry -> entry.getValue().expired(now));
    }

    private String replayKey(KnowledgeEvidenceExchangeRequestBinding binding) {
        return binding.sessionId() + ":" + binding.requestId() + ":" + binding.nonce();
    }
}
