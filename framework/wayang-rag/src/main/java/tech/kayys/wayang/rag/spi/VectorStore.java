package tech.kayys.wayang.rag.spi;

import tech.kayys.wayang.rag.model.RagChunk;
import java.util.List;
import java.util.Map;

/**
 * SPI: pluggable vector database back-end.
 * Implementations (e.g. PgVector, Faiss, Qdrant) live in wayang-rag-runtime.
 */
public interface VectorStore {

    void upsert(String namespace, String id, float[] vector, RagChunk payload, Map<String, Object> metadata);

    List<VectorSearchHit> search(String namespace, float[] queryVector, int topK, double minScore, Map<String, Object> filters);

    boolean delete(String namespace, String id);

    void clear(String namespace);

    default List<VectorSearchHit> keywordSearch(String namespace, String query, int topK, Map<String, Object> filters) {
        return List.of();
    }
}
