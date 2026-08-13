package tech.kayys.wayang.rag.runtime.strategy;

import tech.kayys.wayang.rag.model.MultimodalRetrievalQuery;
import tech.kayys.wayang.rag.model.QueryPart;
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
    public List<RagScoredChunk> retrieve(MultimodalRetrievalQuery query, VectorStore store) {
        float[] queryVector = extractOrEmbed(query);
        if (queryVector == null || queryVector.length == 0) return List.of();
        
        List<VectorSearchHit> hits = store.search("default", queryVector, query.topK(), query.minScore(), query.filters());
        return hits.stream()
                   .map(h -> new RagScoredChunk(h.payload(), h.score()))
                   .toList();
    }

    private float[] extractOrEmbed(MultimodalRetrievalQuery query) {
        StringBuilder text = new StringBuilder();
        for (QueryPart part : query.parts()) {
            if (part instanceof QueryPart.EmbeddingPart e) {
                float[] floats = new float[e.embedding().size()];
                for (int i = 0; i < floats.length; i++) floats[i] = e.embedding().get(i).floatValue();
                return floats;
            } else if (part instanceof QueryPart.TextPart t) {
                text.append(t.text()).append(" ");
            }
        }
        String combinedText = text.toString().trim();
        return combinedText.isEmpty() ? null : embedder.embed(combinedText);
    }
}
