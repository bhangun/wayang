package tech.kayys.wayang.vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.context.Document;
import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultVectorStore implements VectorStore {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private final Map<String, List<Double>> embeddings = new ConcurrentHashMap<>();
    
    public DefaultVectorStore() {
        this.id = Id.random().asString();
        this.name = "default-vector-store";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Vector Store")
            .version(version)
            .label("type", "vector")
            .now()
            .build();
    }
    
    @Override
    public String id() { return id; }
    
    @Override
    public String name() { return name; }
    
    @Override
    public String version() { return version; }
    
    @Override
    public Metadata metadata() { return metadata; }
    
    @Override
    public ResourceType type() { return new ResourceType.Custom("vector"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public void upsert(List<Document> docs) throws Exception {
        for (Document doc : docs) {
            documents.put(doc.id(), doc);
            if (doc.embedding() != null && !doc.embedding().isEmpty()) {
                embeddings.put(doc.id(), doc.embedding());
            }
        }
    }
    
    @Override
    public List<VectorSearchResult> search(VectorSearchQuery query) throws Exception {
        List<VectorSearchResult> results = new ArrayList<>();
        
        if (query.vector() != null && !query.vector().isEmpty()) {
            // Vector search
            for (Map.Entry<String, List<Double>> entry : embeddings.entrySet()) {
                double similarity = cosineSimilarity(query.vector(), entry.getValue());
                if (similarity >= query.minScore()) {
                    Document doc = documents.get(entry.getKey());
                    if (doc != null) {
                        results.add(VectorSearchResult.of(doc, similarity));
                    }
                }
            }
        } else if (query.text() != null) {
            // Text search (simple contains)
            for (Document doc : documents.values()) {
                if (doc.content().toLowerCase().contains(query.text().toLowerCase())) {
                    results.add(VectorSearchResult.of(doc, 0.5));
                }
            }
        }
        
        // Sort by score descending
        results.sort((a, b) -> Double.compare(b.score(), a.score()));
        
        // Apply limit
        if (results.size() > query.limit()) {
            results = results.subList(0, query.limit());
        }
        
        return results;
    }
    
    @Override
    public void delete(String id) throws Exception {
        documents.remove(id);
        embeddings.remove(id);
    }
    
    @Override
    public void deleteByFilter(Map<String, Object> filter) throws Exception {
        // Simple filter implementation
        documents.entrySet().removeIf(entry -> {
            Map<String, Object> metadata = entry.getValue().metadata();
            for (Map.Entry<String, Object> f : filter.entrySet()) {
                if (!f.getValue().equals(metadata.get(f.getKey()))) {
                    return false;
                }
            }
            return true;
        });
        embeddings.keySet().removeIf(id -> !documents.containsKey(id));
    }
    
    @Override
    public Optional<Document> get(String id) throws Exception {
        return Optional.ofNullable(documents.get(id));
    }
    
    @Override
    public long count() throws Exception {
        return documents.size();
    }
    
    @Override
    public void clear() throws Exception {
        documents.clear();
        embeddings.clear();
    }
    
    private double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a.size() != b.size()) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}