package tech.kayys.wayang.rag.spi;

import tech.kayys.wayang.rag.model.RagDocument;
import tech.kayys.wayang.rag.model.RagQuery;
import tech.kayys.wayang.rag.model.RagResult;
import java.util.List;

/**
 * SPI: top-level RAG pipeline contract.
 * Combines document ingestion + retrieval + generation.
 * Implementations live in wayang-rag-runtime.
 */
public interface RagPipeline {

    /** Ingest documents into this pipeline's vector store. */
    void ingest(List<RagDocument> documents);

    /** Execute a RAG query and return the result. */
    RagResult query(RagQuery query);

    /** Clear all documents from this pipeline's namespace. */
    default void clear(String namespace) {}
}
