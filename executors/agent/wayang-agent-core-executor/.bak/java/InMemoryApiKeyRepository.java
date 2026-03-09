package tech.kayys.wayang.agent.repository;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.dto.ApiKeyEntity;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class InMemoryApiKeyRepository implements ApiKeyRepository {
    private final Map<String, ApiKeyEntity> storage = new ConcurrentHashMap<>();

    @Override
    public Uni<ApiKeyEntity> findByKey(String apiKey, String tenantId) {
        return Uni.createFrom().item(storage.get(tenantId + ":" + apiKey));
    }

    @Override
    public Uni<Void> updateLastUsed(String id) {
        // In-memory implementation doesn't strictly need this for now
        return Uni.createFrom().voidItem();
    }

    public void save(ApiKeyEntity entity) {
        storage.put(entity.getTenantId() + ":" + entity.getKey(), entity);
    }
}
