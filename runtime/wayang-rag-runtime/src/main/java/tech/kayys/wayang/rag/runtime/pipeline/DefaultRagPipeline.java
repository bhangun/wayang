package tech.kayys.wayang.rag.runtime.pipeline;

import tech.kayys.wayang.rag.model.*;
import tech.kayys.wayang.rag.runtime.chunker.FixedSizeChunker;
import tech.kayys.wayang.rag.runtime.embedder.NoopEmbedder;
import tech.kayys.wayang.rag.runtime.store.InMemoryVectorStore;
import tech.kayys.wayang.rag.runtime.strategy.DenseRetrievalStrategy;
import tech.kayys.wayang.rag.spi.*;

import java.util.*;

/**
 * Default wiring of the RAG pipeline:
 *   FixedSizeChunker → RagEmbedder → VectorStore → RetrievalStrategy → answer
 *
 * All components are injected so they can be swapped without rewriting the pipeline.
 * The default no-arg constructor assembles a fully in-memory, noop-embedding pipeline
 * suitable for testing or bootstrapping.
 */
public class DefaultRagPipeline implements RagPipeline {

    private static final String DEFAULT_NS = "default";

    private final RagChunker       chunker;
    private final RagEmbedder      embedder;
    private final VectorStore      vectorStore;
    private final RetrievalStrategy strategy;

    /** Builds a fully in-memory, noop-embedding pipeline. */
    public DefaultRagPipeline() {
        this(new FixedSizeChunker(),
             new NoopEmbedder(),
             new InMemoryVectorStore(),
             new DenseRetrievalStrategy());
    }

    public DefaultRagPipeline(RagChunker chunker,
                               RagEmbedder embedder,
                               VectorStore vectorStore,
                               RetrievalStrategy strategy) {
        this.chunker     = Objects.requireNonNull(chunker,     "chunker");
        this.embedder    = Objects.requireNonNull(embedder,    "embedder");
        this.vectorStore = Objects.requireNonNull(vectorStore, "vectorStore");
        this.strategy    = Objects.requireNonNull(strategy,    "strategy");
    }

    @Override
    public void ingest(List<RagDocument> documents) {
        for (RagDocument doc : documents) {
            List<RagChunk> chunks = chunker.chunk(doc);
            for (RagChunk chunk : chunks) {
                float[] vector = embedder.embed(chunk.text());
                vectorStore.upsert(DEFAULT_NS, chunk.id(), vector, chunk, chunk.metadata());
            }
        }
    }

    @Override
    public RagResult query(RagQuery query) {
        MultimodalRetrievalQuery mmQuery = new MultimodalRetrievalQuery(
            List.of(new QueryPart.TextPart(query.text())),
            "hybrid",
            query.topK(),
            query.minScore(),
            Map.of()
        );
        List<RagScoredChunk> retrieved = strategy.retrieve(mmQuery, vectorStore);

        // Simple concatenation answer — override with LLM-based generation as needed
        StringBuilder sb = new StringBuilder();
        for (RagScoredChunk sc : retrieved) {
            if (sc.chunk() != null && sc.chunk().text() != null) {
                sb.append(sc.chunk().text()).append("\n\n");
            }
        }
        String answer = sb.toString().stripTrailing();

        return new RagResult(query, retrieved, answer, Map.of("strategy", strategy.getClass().getSimpleName()));
    }

    @Override
    public void clear(String namespace) {
        vectorStore.clear(namespace != null ? namespace : DEFAULT_NS);
    }
}
