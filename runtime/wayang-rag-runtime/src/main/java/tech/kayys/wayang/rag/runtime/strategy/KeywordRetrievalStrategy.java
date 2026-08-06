package tech.kayys.wayang.rag.runtime.strategy;

import tech.kayys.wayang.rag.model.RagScoredChunk;
import tech.kayys.wayang.rag.spi.RetrievalStrategy;
import tech.kayys.wayang.rag.spi.VectorSearchHit;
import tech.kayys.wayang.rag.spi.VectorStore;

import java.util.List;
import java.util.Map;

/**
 * Lexical (keyword / BM25-like) retrieval strategy.
 * Delegates to VectorStore.keywordSearch().
 */
public class KeywordRetrievalStrategy implements RetrievalStrategy {

    @Override
    public List<RagScoredChunk> retrieve(String query, VectorStore store, int topK, double minScore) {
        List<VectorSearchHit> hits = store.keywordSearch("default", query, topK, Map.of());
        return hits.stream()
                   .filter(h -> h.score() >= minScore)
                   .map(h -> new RagScoredChunk(h.payload(), h.score()))
                   .toList();
    }
}
