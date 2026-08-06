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
import java.util.Map;
import java.util.Objects;

import tech.kayys.wayang.extension.Id;

/**
 * Memory Record - complete memory record model
 */
public record MemoryRecord(
    String id,
    String type,
    String key,
    String value,
    Map<String, Object> metadata,
    Instant timestamp,
    double relevance,
    long ttlSeconds,
    String sessionId,
    String userId
) {
    public MemoryRecord {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (metadata == null) {
            metadata = Map.of();
        }
        if (id == null) {
            id = Id.random().asString();
        }
    }
    
    public static MemoryRecord of(String key, String value) {
        return new MemoryRecord(
            Id.random().asString(),
            "default",
            key,
            value,
            Map.of(),
            Instant.now(),
            1.0,
            3600,
            null,
            null
        );
    }
    
    public static MemoryRecord of(String key, String value, String type) {
        return new MemoryRecord(
            Id.random().asString(),
            type,
            key,
            value,
            Map.of(),
            Instant.now(),
            1.0,
            3600,
            null,
            null
        );
    }
    
    public MemoryRecord withMetadata(Map<String, Object> metadata) {
        return new MemoryRecord(id, type, key, value, metadata, timestamp, 
            relevance, ttlSeconds, sessionId, userId);
    }
    
    public MemoryRecord withRelevance(double relevance) {
        return new MemoryRecord(id, type, key, value, metadata, timestamp, 
            relevance, ttlSeconds, sessionId, userId);
    }
}