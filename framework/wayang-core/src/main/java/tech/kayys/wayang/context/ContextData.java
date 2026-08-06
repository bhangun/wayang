package tech.kayys.wayang.context;
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

import tech.kayys.wayang.extension.Id;



/**
 * Context Data - complete context data model
 */
public record ContextData(
    String id,
    String query,
    String originalQuery,
    List<Document> documents,
    List<Object> memories,
    List<Object> knowledge,
    Map<String, Object> structuredData,
    Map<String, Object> metadata,
    double relevanceScore,
    long retrievalTimeMs,
    String contextType
) {
    public static ContextData empty() {
        return new ContextData(
            Id.random().asString(),
            null,
            null,
            List.of(),
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            0.0,
            0,
            "none"
        );
    }
    
    public static ContextData ofDocuments(List<Document> documents) {
        return new ContextData(
            Id.random().asString(),
            null,
            null,
            documents,
            List.of(),
            List.of(),
            Map.of(),
            Map.of(),
            1.0,
            0,
            "vector_rag"
        );
    }
    
    public boolean isEmpty() {
        return documents.isEmpty() && memories.isEmpty() && knowledge.isEmpty() && structuredData.isEmpty();
    }
    
    public ContextData withDocuments(List<Document> documents) {
        return new ContextData(id, query, originalQuery, documents, memories, knowledge, 
            structuredData, metadata, relevanceScore, retrievalTimeMs, contextType);
    }
    
    public ContextData withMemories(List<Object> memories) {
        return new ContextData(id, query, originalQuery, documents, memories, knowledge, 
            structuredData, metadata, relevanceScore, retrievalTimeMs, contextType);
    }
    
    public ContextData withKnowledge(List<Object> knowledge) {
        return new ContextData(id, query, originalQuery, documents, memories, knowledge, 
            structuredData, metadata, relevanceScore, retrievalTimeMs, contextType);
    }
}
