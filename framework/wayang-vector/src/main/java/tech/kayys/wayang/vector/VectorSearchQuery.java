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

import tech.kayys.wayang.extension.Id;

/**
 * Vector Search Query
 */
public record VectorSearchQuery(
    String id,
    List<Double> vector,
    String text,
    Map<String, Object> filter,
    int limit,
    double minScore,
    List<String> fields
) {
    public static VectorSearchQuery of(List<Double> vector, int limit) {
        return new VectorSearchQuery(
            Id.random().asString(),
            vector,
            null,
            Map.of(),
            limit,
            0.0,
            List.of()
        );
    }
    
    public static VectorSearchQuery ofText(String text, int limit) {
        return new VectorSearchQuery(
            Id.random().asString(),
            null,
            text,
            Map.of(),
            limit,
            0.0,
            List.of()
        );
    }
    
    public VectorSearchQuery withFilter(Map<String, Object> filter) {
        return new VectorSearchQuery(id, vector, text, filter, limit, minScore, fields);
    }
    
    public VectorSearchQuery withMinScore(double minScore) {
        return new VectorSearchQuery(id, vector, text, filter, limit, minScore, fields);
    }
}