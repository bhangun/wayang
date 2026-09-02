package tech.kayys.wayang.knowledge.exchange.envelope;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryKnowledgeEvidenceExchangeKeyResolver
        implements KnowledgeEvidenceExchangeKeyResolver {

    private final Map<String, KnowledgeEvidenceExchangeMessageAuthenticator> keys =
            new ConcurrentHashMap<>();

    public void register(KnowledgeEvidenceExchangeMessageAuthenticator authenticator) {
        String key = key(authenticator.keyId(), authenticator.keyVersion());
        keys.put(key, authenticator);
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeMessageAuthenticator> resolveSigner(
            String keyId,
            String keyVersion,
            Instant now
    ) {
        return Optional.ofNullable(keys.get(key(keyId, keyVersion)));
    }

    @Override
    public Optional<KnowledgeEvidenceExchangeMessageAuthenticator> resolveVerifier(
            String keyId,
            String keyVersion,
            Instant now
    ) {
        return Optional.ofNullable(keys.get(key(keyId, keyVersion)));
    }

    private String key(String keyId, String keyVersion) {
        return keyId + ":" + keyVersion;
    }
}
