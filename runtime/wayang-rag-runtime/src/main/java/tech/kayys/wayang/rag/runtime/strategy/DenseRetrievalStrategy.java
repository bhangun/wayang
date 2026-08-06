package tech.kayys.wayang.rag.runtime.strategy;

import tech.kayys.wayang.rag.model.RagChunk;
import tech.kayys.wayang.rag.model.RagScoredChunk;
import tech.kayys.wayang.rag.runtime.embedder.NoopEmbedder;
import tech.kayys.wayang.rag.spi.RagEmbedder;
import tech.kayys.wayang.rag.spi.RetrievalStrategy;
import tech.kayys.wayang.rag.spi.VectorSearchHit;
import tech.kayys.wayang.rag.spi.VectorStore;

import java.util.List;
import java.util.Map;

/**
 * Dense (vector similarity) retrieval strategy.
 */
public class DenseRetrievalStrategy implements RetrievalStrategy {

    private final RagEmbedder embedder;

    public DenseRetrievalStrategy() {
        this(new NoopEmbedder());
    }

    public DenseRetrievalStrategy(RagEmbedder embedder) {
        this.embedder = embedder;
    }

    @Override
    public List<RagScoredChunk> retrieve(String query, VectorStore store, int topK, double minScore) {
        float[] queryVector = embedder.embed(query);
        List<VectorSearchHit> hits = store.search("default", queryVector, topK, minScore, Map.of());
        return hits.stream()
                   .map(h -> new RagScoredChunk(h.payload(), h.score()))
                   .toList();
    }
}
