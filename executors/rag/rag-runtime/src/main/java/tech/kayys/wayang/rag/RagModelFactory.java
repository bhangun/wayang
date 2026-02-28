package tech.kayys.wayang.rag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.embedding.EmbeddingService;

@ApplicationScoped
public class RagModelFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RagModelFactory.class);

    @Inject
    RagRuntimeConfig config;

    @Inject
    EmbeddingService embeddingService;

    @Inject
    RagObservabilityMetrics metrics;

    public RagEmbeddingModel createEmbeddingModel(String tenantId, String modelName) {
        LOG.debug("Creating embedding model {} for tenant {}", modelName, tenantId);
        String mappedModel = mapModel(modelName);
        return new OwnedEmbeddingModelAdapter(embeddingService, tenantId, mappedModel, metrics);
    }

    public Object createChatModel(String tenantId, String modelName) {
        LOG.debug("Creating chat model {} for tenant {}", modelName, tenantId);
        return null;
    }

    private String mapModel(String requestedModel) {
        if (requestedModel == null || requestedModel.isBlank()) {
            return "hash-" + config.getEmbeddingDimension();
        }

        String normalized = requestedModel.trim().toLowerCase();
        if (normalized.startsWith("hash-")
                || normalized.startsWith("tfidf-")
                || normalized.startsWith("chargram-")
                || normalized.equals("hash")
                || normalized.equals("tfidf")
                || normalized.equals("chargram")) {
            return normalized;
        }

        if (normalized.contains("embedding")) {
            return "hash-" + config.getEmbeddingDimension();
        }
        return "tfidf-" + Math.max(256, config.getEmbeddingDimension() / 2);
    }
}
