package main.java.tech.kayys.wayang.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.executor.rag.retrieval.RetrievalConfig;
import tech.kayys.gamelan.executor.rag.retrieval.ScoredDocument;
import tech.kayys.gamelan.executor.rag.embedding.EmbeddingModelFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * EMBEDDING STORE REGISTRY - FULL IMPLEMENTATION
 */
@ApplicationScoped
public class EmbeddingStoreRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddingStoreRegistry.class);

    private final Map<String, Map<String, EmbeddingStore<TextSegment>>> stores = new ConcurrentHashMap<>();

    @ConfigProperty(name = "gamelan.rag.store.default-type", defaultValue = "in-memory")
    String defaultStoreType;

    @ConfigProperty(name = "gamelan.rag.store.pgvector.host", defaultValue = "localhost")
    String pgvectorHost;

    @ConfigProperty(name = "gamelan.rag.store.pgvector.port", defaultValue = "5432")
    int pgvectorPort;

    @ConfigProperty(name = "gamelan.rag.store.pgvector.database", defaultValue = "gamelan")
    String pgvectorDatabase;

    @ConfigProperty(name = "gamelan.rag.store.pgvector.user", defaultValue = "postgres")
    String pgvectorUser;

    @ConfigProperty(name = "gamelan.rag.store.pgvector.password", defaultValue = "")
    Optional<String> pgvectorPassword;

    @ConfigProperty(name = "gamelan.rag.store.redis.host", defaultValue = "localhost")
    String redisHost;

    @ConfigProperty(name = "gamelan.rag.store.redis.port", defaultValue = "6379")
    int redisPort;

    public EmbeddingStore<TextSegment> getStore(String tenantId, String storeType) {
        LOG.debug("Getting embedding store for tenant: {}, type: {}", tenantId, storeType);

        return stores
                .computeIfAbsent(tenantId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(storeType, k -> createStore(tenantId, storeType));
    }

    private EmbeddingStore<TextSegment> createStore(String tenantId, String storeType) {
        LOG.info("Creating embedding store: tenant={}, type={}", tenantId, storeType);

        return switch (storeType.toLowerCase()) {
            case "in-memory" -> new InMemoryEmbeddingStore<>();
            case "pgvector" -> createPgVectorStore(tenantId);
            case "redis" -> createRedisStore(tenantId);
            default -> {
                LOG.warn("Unknown store type: {}, using in-memory", storeType);
                yield new InMemoryEmbeddingStore<>();
            }
        };
    }

    private EmbeddingStore<TextSegment> createPgVectorStore(String tenantId) {
        try {
            return PgVectorEmbeddingStore.builder()
                    .host(pgvectorHost)
                    .port(pgvectorPort)
                    .database(pgvectorDatabase)
                    .user(pgvectorUser)
                    .password(pgvectorPassword.orElse(""))
                    .table("embeddings_" + sanitizeTenantId(tenantId))
                    .dimension(1536)
                    .createTable(true)
                    .dropTableFirst(false)
                    .build();
        } catch (Exception e) {
            LOG.warn("Failed to create PgVector store, falling back to in-memory: {}", e.getMessage());
            return new InMemoryEmbeddingStore<>();
        }
    }

    private EmbeddingStore<TextSegment> createRedisStore(String tenantId) {
        try {
            return RedisEmbeddingStore.builder()
                    .host(redisHost)
                    .port(redisPort)
                    .indexName("embeddings:" + tenantId)
                    .dimension(1536)
                    .build();
        } catch (Exception e) {
            LOG.warn("Failed to create Redis store, falling back to in-memory: {}", e.getMessage());
            return new InMemoryEmbeddingStore<>();
        }
    }

    public void clearStore(String tenantId, String storeType) {
        LOG.info("Clearing store for tenant: {}, type: {}", tenantId, storeType);
        Map<String, EmbeddingStore<TextSegment>> tenantStores = stores.get(tenantId);
        if (tenantStores != null) {
            tenantStores.remove(storeType);
        }
    }

    public void clearAllStores(String tenantId) {
        LOG.info("Clearing all stores for tenant: {}", tenantId);
        stores.remove(tenantId);
    }

    private String sanitizeTenantId(String tenantId) {
        return tenantId.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }
}