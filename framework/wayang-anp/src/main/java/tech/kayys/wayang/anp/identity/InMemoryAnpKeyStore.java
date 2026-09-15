package tech.kayys.wayang.anp.identity;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory thread-safe implementation of {@link AnpKeyStore}.
 */
public final class InMemoryAnpKeyStore implements AnpKeyStore {

    private final Map<String, AnpIdentityKeyPair> keysById = new ConcurrentHashMap<>();
    private final Map<String, String> keyIdByAgent = new ConcurrentHashMap<>();

    @Override
    public Optional<AnpIdentityKeyPair> getKey(String keyId) {
        return Optional.ofNullable(keysById.get(keyId));
    }

    @Override
    public Optional<String> getAgentKeyId(String agentId) {
        return Optional.ofNullable(keyIdByAgent.get(agentId));
    }

    @Override
    public void storeKey(String agentId, AnpIdentityKeyPair keyPair) {
        keysById.put(keyPair.keyId(), keyPair);
        keyIdByAgent.put(agentId, keyPair.keyId());
    }
}
