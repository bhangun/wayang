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


import java.util.HashMap;
import java.util.Map;

import tech.kayys.wayang.extension.Id;

/**
 * Knowledge Request
 */
public record KnowledgeRequest(
    String id,
    String query,
    String type,
    Map<String, Object> filters,
    int limit,
    double minScore,
    boolean includeMetadata
) {
    public static KnowledgeRequest of(String query) {
        return new KnowledgeRequest(
            Id.random().asString(),
            query,
            null,
            Map.of(),
            10,
            0.0,
            true
        );
    }
    
    public KnowledgeRequest withFilter(String key, Object value) {
        Map<String, Object> newFilters = new HashMap<>(filters);
        newFilters.put(key, value);
        return new KnowledgeRequest(id, query, type, newFilters, limit, minScore, includeMetadata);
    }
}
