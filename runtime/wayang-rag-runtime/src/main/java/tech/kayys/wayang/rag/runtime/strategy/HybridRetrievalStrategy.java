package tech.kayys.wayang.rag.runtime.strategy;

import tech.kayys.wayang.rag.model.MultimodalRetrievalQuery;
import tech.kayys.wayang.rag.model.RagScoredChunk;
import tech.kayys.wayang.rag.spi.RagEmbedder;
import tech.kayys.wayang.rag.spi.RetrievalStrategy;
import tech.kayys.wayang.rag.spi.VectorStore;
import tech.kayys.wayang.rag.runtime.embedder.NoopEmbedder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Combines dense + keyword retrieval (Reciprocal Rank Fusion).
 */
public class HybridRetrievalStrategy implements RetrievalStrategy {

    private final RetrievalStrategy dense;
    private final RetrievalStrategy keyword;
    private final double alpha; // 0.0 = full keyword, 1.0 = full dense

    public HybridRetrievalStrategy() {
        this(new DenseRetrievalStrategy(), new KeywordRetrievalStrategy(), 0.7);
    }

    public HybridRetrievalStrategy(RetrievalStrategy dense, RetrievalStrategy keyword, double alpha) {
        this.dense   = dense;
        this.keyword = keyword;
        this.alpha   = Math.max(0.0, Math.min(1.0, alpha));
    }

    @Override
    public List<RagScoredChunk> retrieve(MultimodalRetrievalQuery query, VectorStore store) {
        MultimodalRetrievalQuery subQuery = new MultimodalRetrievalQuery(query.parts(), query.mode(), query.topK() * 2, 0.0, query.filters());
        List<RagScoredChunk> denseHits   = dense.retrieve(subQuery, store);
        List<RagScoredChunk> keywordHits = keyword.retrieve(subQuery, store);

        // Reciprocal Rank Fusion
        Map<String, Double> scores = new LinkedHashMap<>();
        int k = 60; // RRF constant
        accumulate(denseHits,   scores, alpha,        k);
        accumulate(keywordHits, scores, (1.0 - alpha), k);

        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(query.topK())
                .map(e -> {
                    // Find the chunk matching this id
                    return denseHits.stream()
                            .filter(c -> c.chunk() != null && c.chunk().id().equals(e.getKey()))
                            .findFirst()
                            .orElseGet(() -> keywordHits.stream()
                                    .filter(c -> c.chunk() != null && c.chunk().id().equals(e.getKey()))
                                    .findFirst().orElse(null));
                })
                .filter(Objects::nonNull)
                .filter(c -> c.score() >= query.minScore())
                .toList();
    }

    private void accumulate(List<RagScoredChunk> hits, Map<String, Double> scores, double weight, int k) {
        for (int rank = 0; rank < hits.size(); rank++) {
            RagScoredChunk sc = hits.get(rank);
            if (sc.chunk() == null) continue;
            String id = sc.chunk().id();
            scores.merge(id, weight / (k + rank + 1), Double::sum);
        }
    }
}
