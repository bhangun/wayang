package tech.kayys.wayang.embedding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import tech.kayys.wayang.core.Id;
import tech.kayys.wayang.core.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

public class DefaultEmbeddingProvider implements EmbeddingProvider {
    
    private final String id;
    private final String name;
    private final String version;
    private final Metadata metadata;
    private final int dimension = 768;
    
    public DefaultEmbeddingProvider() {
        this.id = Id.random().asString();
        this.name = "default-embedding-provider";
        this.version = "1.0.0";
        this.metadata = Metadata.builder()
            .name(name)
            .description("Default Embedding Provider")
            .version(version)
            .label("type", "embedding")
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
    public ResourceType type() { return new ResourceType.Custom("embedding"); }
    
    @Override
    public ResourceId resourceId() { return new ResourceId.CustomId(Id.fromString(id), type()); }
    
    @Override
    public EmbeddingVector embed(String text) throws Exception {
        // Generate a deterministic pseudo-embedding based on text hash
        List<Double> vector = new ArrayList<>();
        int hash = text.hashCode();
        Random random = new Random(hash);
        
        for (int i = 0; i < dimension; i++) {
            vector.add(random.nextDouble() * 2 - 1);
        }
        
        // Normalize
        double norm = Math.sqrt(vector.stream().mapToDouble(v -> v * v).sum());
        if (norm > 0) {
            vector = vector.stream().map(v -> v / norm).collect(Collectors.toList());
        }
        
        return EmbeddingVector.of(text, vector);
    }
    
    @Override
    public EmbeddingModelInfo getModelInfo() {
        return new EmbeddingModelInfo("default", dimension, "wayang");
    }
}
