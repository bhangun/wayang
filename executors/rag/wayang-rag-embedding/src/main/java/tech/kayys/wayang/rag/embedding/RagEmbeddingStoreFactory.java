package tech.kayys.wayang.rag.embedding;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.gamelan.executor.rag.domain.RetrievalConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class RagEmbeddingStoreFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RagEmbeddingStoreFactory.class);

    @Inject
    RagRuntimeConfig config;

    @Inject
    RagVectorStoreProvider ragVectorStoreProvider;

    @Inject
    RagObservabilityMetrics metrics;

    private final Map<String, RagEmbeddingStore> cache = new ConcurrentHashMap<>();
    private final Map<String, EmbeddingSchemaContract> contracts = new ConcurrentHashMap<>();

    public RagEmbeddingStore getStore(String tenantId, RetrievalConfig retrievalConfig) {
        LOG.debug("Getting embedding store for tenant {}", tenantId);
        EmbeddingSchemaContract contract = contracts.computeIfAbsent(tenantId, ignored -> defaultContract());
        return cache.computeIfAbsent(
                tenantId,
                namespace -> new OwnedRagEmbeddingStoreAdapter(
                        namespace,
                        contract.model(),
                        contract.dimension(),
                        contract.version(),
                        ragVectorStoreProvider.getStore(),
                        metrics));
    }

    public EmbeddingSchemaContract contractForTenant(String tenantId) {
        return contracts.getOrDefault(tenantId, defaultContract());
    }

    public synchronized EmbeddingSchemaContract migrateTenantContract(
            String tenantId,
            String embeddingModel,
            int embeddingDimension,
            String embeddingVersion,
            boolean clearNamespace) {

        EmbeddingSchemaContract target = new EmbeddingSchemaContract(
                embeddingModel,
                embeddingDimension,
                embeddingVersion);
        EmbeddingSchemaContract previous = contractForTenant(tenantId);
        boolean changed = !previous.equals(target);
        if (changed && !clearNamespace) {
            throw new IllegalArgumentException("clearNamespace must be true when embedding contract changes");
        }

        RagEmbeddingStore existing = cache.remove(tenantId);
        if (clearNamespace) {
            if (existing != null) {
                existing.clear();
            } else {
                ragVectorStoreProvider.getStore().clear(tenantId);
            }
        }

        contracts.put(tenantId, target);
        cache.put(tenantId, new OwnedRagEmbeddingStoreAdapter(
                tenantId,
                target.model(),
                target.dimension(),
                target.version(),
                ragVectorStoreProvider.getStore(),
                metrics));
        return previous;
    }

    private EmbeddingSchemaContract defaultContract() {
        String version = config.getEmbeddingVersion();
        if (version == null || version.isBlank()) {
            version = "v1";
        }
        return new EmbeddingSchemaContract(
                config.getEmbeddingModel(),
                config.getEmbeddingDimension(),
                version);
    }
}
