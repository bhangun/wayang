package tech.kayys.wayang.rag.core;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RETRIEVAL STRATEGY FACTORY - FULL IMPLEMENTATION
 */
@ApplicationScoped
public class RetrievalStrategyFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RetrievalStrategyFactory.class);

    @Inject
    EmbeddingModelFactory embeddingModelFactory;

    public RetrievalStrategy getStrategy(String strategyType) {
        LOG.debug("Creating retrieval strategy: {}", strategyType);

        return switch (strategyType.toLowerCase()) {
            case "dense" -> new DenseRetrievalStrategy(embeddingModelFactory);
            case "hybrid" -> new HybridRetrievalStrategy(embeddingModelFactory);
            case "keyword" -> new KeywordRetrievalStrategy();
            default -> {
                LOG.warn("Unknown strategy: {}, using dense", strategyType);
                yield new DenseRetrievalStrategy(embeddingModelFactory);
            }
        };
    }
}
