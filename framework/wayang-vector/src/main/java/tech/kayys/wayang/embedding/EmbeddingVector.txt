package tech.kayys.wayang.embedding;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.time.Instant;
import java.util.List;

import tech.kayys.wayang.extension.Id;

/**
 * Embedding Vector
 */
public record EmbeddingVector(
    String id,
    String text,
    List<Double> vector,
    int dimension,
    Instant timestamp
) {
    public static EmbeddingVector of(String text, List<Double> vector) {
        return new EmbeddingVector(
            Id.random().asString(),
            text,
            vector,
            vector.size(),
            Instant.now()
        );
    }
    
    public double cosineSimilarity(EmbeddingVector other) {
        if (vector.size() != other.vector.size()) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vector.size(); i++) {
            dot += vector.get(i) * other.vector().get(i);
            normA += vector.get(i) * vector.get(i);
            normB += other.vector().get(i) * other.vector().get(i);
        }
        if (normA == 0 || normB == 0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
