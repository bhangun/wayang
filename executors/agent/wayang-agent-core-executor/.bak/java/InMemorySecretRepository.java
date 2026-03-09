package tech.kayys.wayang.agent.repository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.dto.SecretEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemorySecretRepository implements SecretRepository {
    private final Map<String, SecretEntity> storage = new ConcurrentHashMap<>();

    @Override
    public Uni<SecretEntity> findByKey(String key, String tenantId) {
        return Uni.createFrom().item(storage.get(tenantId + ":" + key));
    }

    @Override
    public Uni<Void> save(String key, String encryptedValue, String tenantId) {
        storage.put(tenantId + ":" + key, new SecretEntity(key, encryptedValue, tenantId));
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<Void> delete(String key, String tenantId) {
        storage.remove(tenantId + ":" + key);
        return Uni.createFrom().voidItem();
    }
}
