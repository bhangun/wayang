package tech.kayys.wayang.agent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.resource.Artifact;

/**
 * Agent Response - complete response model for agents
 */
public record AgentResponse(
    String id,
    String sessionId,
    String content,
    String type,
    List<Artifact> artifacts,
    Map<String, Object> metadata,
    Map<String, Object> data,
    boolean success,
    String error,
    long responseTimeMs,
    Instant timestamp
) {
    public static AgentResponseBuilder builder() {
        return new AgentResponseBuilder();
    }
    
    public static AgentResponse of(String content) {
        return new AgentResponse(
            Id.random().asString(),
            null,
            content,
            "text",
            List.of(),
            Map.of(),
            Map.of(),
            true,
            null,
            0,
            Instant.now()
        );
    }
    
    public static AgentResponse success(String content) {
        return new AgentResponse(
            Id.random().asString(),
            null,
            content,
            "text",
            List.of(),
            Map.of(),
            Map.of(),
            true,
            null,
            0,
            Instant.now()
        );
    }
    
    public static AgentResponse failure(String error) {
        return new AgentResponse(
            Id.random().asString(),
            null,
            null,
            "error",
            List.of(),
            Map.of(),
            Map.of(),
            false,
            error,
            0,
            Instant.now()
        );
    }
    
    public AgentResponse withArtifact(Artifact artifact) {
        List<Artifact> newArtifacts = new ArrayList<>(artifacts);
        newArtifacts.add(artifact);
        return new AgentResponse(id, sessionId, content, type, newArtifacts, 
            metadata, data, success, error, responseTimeMs, timestamp);
    }
    
    public AgentResponse withData(String key, Object value) {
        Map<String, Object> newData = new HashMap<>(data);
        newData.put(key, value);
        return new AgentResponse(id, sessionId, content, type, artifacts, 
            metadata, newData, success, error, responseTimeMs, timestamp);
    }
    
    public AgentResponse withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(metadata);
        newMetadata.put(key, value);
        return new AgentResponse(id, sessionId, content, type, artifacts, 
            newMetadata, data, success, error, responseTimeMs, timestamp);
    }
    
    public static class AgentResponseBuilder {
        private String id;
        private String sessionId;
        private String content;
        private String type = "text";
        private final List<Artifact> artifacts = new ArrayList<>();
        private final Map<String, Object> metadata = new HashMap<>();
        private final Map<String, Object> data = new HashMap<>();
        private boolean success = true;
        private String error;
        private long responseTimeMs;
        private Instant timestamp;
        
        public AgentResponseBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public AgentResponseBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public AgentResponseBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public AgentResponseBuilder type(String type) {
            this.type = type;
            return this;
        }
        
        public AgentResponseBuilder artifact(Artifact artifact) {
            this.artifacts.add(artifact);
            return this;
        }
        
        public AgentResponseBuilder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public AgentResponseBuilder data(String key, Object value) {
            this.data.put(key, value);
            return this;
        }
        
        public AgentResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public AgentResponseBuilder error(String error) {
            this.error = error;
            this.success = false;
            return this;
        }
        
        public AgentResponseBuilder responseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
            return this;
        }
        
        public AgentResponseBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public AgentResponse build() {
            if (id == null) {
                id = Id.random().asString();
            }
            if (timestamp == null) {
                timestamp = Instant.now();
            }
            return new AgentResponse(id, sessionId, content, type, artifacts, 
                metadata, data, success, error, responseTimeMs, timestamp);
        }
    }
}
