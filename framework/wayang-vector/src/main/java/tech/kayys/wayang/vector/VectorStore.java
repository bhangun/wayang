package tech.kayys.wayang.vector;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;
import java.util.Map;
import java.util.Optional;

import tech.kayys.wayang.embedding.EmbeddingVector;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.context.Document;

/**
 * Vector Store - stores and searches vectors.
 */
public interface VectorStore extends Extension {
    
    /**
     * Upsert documents
     */
    void upsert(List<Document> documents) throws Exception;
    
    /**
     * Upsert with embeddings
     */
    default void upsertWithEmbeddings(List<EmbeddingVector> embeddings) throws Exception {
        // Convert to documents and upsert
        List<Document> documents = embeddings.stream()
            .map(e -> new Document(e.id(), e.text(), null, null, 
                Map.of("embedding", e.vector()), e.vector()))
            .toList();
        upsert(documents);
    }
    
    /**
     * Search vectors
     */
    List<VectorSearchResult> search(VectorSearchQuery query) throws Exception;
    
    /**
     * Delete by ID
     */
    void delete(String id) throws Exception;
    
    /**
     * Delete by filter
     */
    void deleteByFilter(Map<String, Object> filter) throws Exception;
    
    /**
     * Get document
     */
    Optional<Document> get(String id) throws Exception;
    
    /**
     * Count documents
     */
    default long count() throws Exception {
        return 0;
    }
    
    /**
     * Clear all
     */
    default void clear() throws Exception {
        // Optional
    }
}
