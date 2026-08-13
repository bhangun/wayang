package tech.kayys.wayang.rag.spi;

import tech.kayys.wayang.rag.model.RagChunk;
import tech.kayys.wayang.rag.model.RagScoredChunk;
import tech.kayys.wayang.rag.model.MultimodalRetrievalQuery;
import java.util.List;

/**
 * SPI: strategy for retrieving relevant chunks from a vector store.
 * Implementations live in wayang-rag-runtime.
 */
public interface RetrievalStrategy {
    List<RagScoredChunk> retrieve(MultimodalRetrievalQuery query, VectorStore store);
}
