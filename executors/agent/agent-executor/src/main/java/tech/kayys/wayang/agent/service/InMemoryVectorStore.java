package tech.kayys.wayang.agent.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.agent.dto.SimilarMessage;
import tech.kayys.wayang.agent.dto.VectorStoreStats;
import tech.kayys.wayang.agent.model.Message;
import tech.kayys.wayang.agent.model.VectorStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
@jakarta.inject.Named("in-memory")
@jakarta.enterprise.inject.Alternative
public class InMemoryVectorStore implements VectorStore {

    private final Map<String, List<SimilarMessage>> storage = new ConcurrentHashMap<>();

    @Override
    public Uni<String> store(String sessionId, String tenantId, Message message, float[] embedding) {
        String id = UUID.randomUUID().toString();
        String key = tenantId + ":" + sessionId;

        List<SimilarMessage> messages = storage.computeIfAbsent(key, k -> new ArrayList<>());
        messages.add(new SimilarMessage(id, message, 1.0, Map.of()));

        return Uni.createFrom().item(id);
    }

    @Override
    public Uni<List<SimilarMessage>> search(String sessionId, String tenantId, float[] queryEmbedding, int limit) {
        String key = tenantId + ":" + sessionId;
        List<SimilarMessage> messages = storage.getOrDefault(key, List.of());
        return Uni.createFrom().item(messages.stream().limit(limit).toList());
    }

    @Override
    public Uni<List<SimilarMessage>> searchWithFilter(String sessionId, String tenantId, float[] queryEmbedding,
            Map<String, Object> filters, int limit) {
        return search(sessionId, tenantId, queryEmbedding, limit);
    }

    @Override
    public Uni<Void> deleteSession(String sessionId, String tenantId) {
        storage.remove(tenantId + ":" + sessionId);
        return Uni.createFrom().voidItem();
    }

    @Override
    public Uni<VectorStoreStats> getStats(String tenantId) {
        long count = storage.values().stream().mapToLong(List::size).sum();
        return Uni.createFrom().item(new VectorStoreStats(count, 1536, "in-memory"));
    }
}
