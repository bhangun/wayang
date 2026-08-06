package tech.kayys.wayang.session;
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
import java.util.HashMap;
import java.util.Map;

import tech.kayys.wayang.extension.Id;

/**
 * Agent Session
 */
public record AgentSession(
    String id,
    String userId,
    String tenantId,
    Map<String, Object> data,
    Instant createdAt,
    Instant lastAccessedAt,
    Instant expiresAt,
    SessionStatus status
) {
    public static AgentSession of(String userId) {
        Instant now = Instant.now();
        return new AgentSession(
            Id.random().asString(),
            userId,
            null,
            Map.of(),
            now,
            now,
            now.plusSeconds(3600),
            SessionStatus.ACTIVE
        );
    }
    
    public AgentSession withData(String key, Object value) {
        Map<String, Object> newData = new HashMap<>(data);
        newData.put(key, value);
        return new AgentSession(id, userId, tenantId, newData, createdAt, 
            lastAccessedAt, expiresAt, status);
    }
    
    public AgentSession withStatus(SessionStatus status) {
        return new AgentSession(id, userId, tenantId, data, createdAt, 
            lastAccessedAt, expiresAt, status);
    }
    
    public AgentSession touch() {
        return new AgentSession(id, userId, tenantId, data, createdAt, 
            Instant.now(), expiresAt, status);
    }
}
