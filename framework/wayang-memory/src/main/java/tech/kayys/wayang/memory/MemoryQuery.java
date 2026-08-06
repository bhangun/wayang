package tech.kayys.wayang.memory;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;


/**
 * Memory Query - complete query model
 */
public record MemoryQuery(
    String id,
    String query,
    String type,
    List<String> keys,
    Map<String, Object> filters,
    int limit,
    int offset,
    String sortBy,
    boolean ascending,
    double minRelevance,
    Instant from,
    Instant to
) {
    public static MemoryQueryBuilder builder() {
        return new MemoryQueryBuilder();
    }
    
    public static MemoryQuery of(String query) {
        return new MemoryQuery(
            Id.random().asString(),
            query,
            null,
            List.of(),
            Map.of(),
            10,
            0,
            null,
            true,
            0.0,
            null,
            null
        );
    }
    
    public static class MemoryQueryBuilder {
        private String id;
        private String query;
        private String type;
        private final List<String> keys = new ArrayList<>();
        private final Map<String, Object> filters = new HashMap<>();
        private int limit = 10;
        private int offset = 0;
        private String sortBy;
        private boolean ascending = true;
        private double minRelevance = 0.0;
        private Instant from;
        private Instant to;
        
        public MemoryQueryBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public MemoryQueryBuilder query(String query) {
            this.query = query;
            return this;
        }
        
        public MemoryQueryBuilder type(String type) {
            this.type = type;
            return this;
        }
        
        public MemoryQueryBuilder key(String key) {
            this.keys.add(key);
            return this;
        }
        
        public MemoryQueryBuilder filter(String key, Object value) {
            this.filters.put(key, value);
            return this;
        }
        
        public MemoryQueryBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }
        
        public MemoryQueryBuilder offset(int offset) {
            this.offset = offset;
            return this;
        }
        
        public MemoryQueryBuilder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }
        
        public MemoryQueryBuilder ascending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }
        
        public MemoryQueryBuilder minRelevance(double minRelevance) {
            this.minRelevance = minRelevance;
            return this;
        }
        
        public MemoryQueryBuilder from(Instant from) {
            this.from = from;
            return this;
        }
        
        public MemoryQueryBuilder to(Instant to) {
            this.to = to;
            return this;
        }
        
        public MemoryQuery build() {
            if (id == null) {
                id = Id.random().asString();
            }
            return new MemoryQuery(id, query, type, keys, filters, limit, offset, 
                sortBy, ascending, minRelevance, from, to);
        }
    }
}
