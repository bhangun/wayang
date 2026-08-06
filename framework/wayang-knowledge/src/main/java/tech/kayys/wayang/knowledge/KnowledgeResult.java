package tech.kayys.wayang.knowledge;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.context.Document;
import tech.kayys.wayang.extension.Id;

/**
 * Knowledge Result
 */
public record KnowledgeResult(
    String id,
    String query,
    List<Document> documents,
    List<KnowledgeFact> facts,
    Map<String, Object> metadata,
    long retrievalTimeMs,
    double overallScore
) {
    public static KnowledgeResult empty(String query) {
        return new KnowledgeResult(
            Id.random().asString(),
            query,
            List.of(),
            List.of(),
            Map.of(),
            0,
            0.0
        );
    }
    
    public KnowledgeResult withDocument(Document document) {
        List<Document> newDocuments = new ArrayList<>(documents);
        newDocuments.add(document);
        return new KnowledgeResult(id, query, newDocuments, facts, metadata, 
            retrievalTimeMs, overallScore);
    }
    
    public KnowledgeResult withFact(KnowledgeFact fact) {
        List<KnowledgeFact> newFacts = new ArrayList<>(facts);
        newFacts.add(fact);
        return new KnowledgeResult(id, query, documents, newFacts, metadata, 
            retrievalTimeMs, overallScore);
    }
}