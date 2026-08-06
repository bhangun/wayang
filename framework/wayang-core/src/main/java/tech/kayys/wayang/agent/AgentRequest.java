package tech.kayys.wayang.agent;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.input.InputType;
import tech.kayys.wayang.resource.Artifact;

/**
 * Agent Request - complete request model
 */
public record AgentRequest(
    String id,
    String sessionId,
    String tenantId,
    String userId,
    InputType type,
    String content,
    Map<String, Object> metadata,
    List<Artifact> artifacts,
    Map<String, Object> parameters,
    Instant timestamp
) {
    public static AgentRequestBuilder builder() {
        return new AgentRequestBuilder();
    }
    
    public static AgentRequest of(String content) {
        return new AgentRequest(
            Id.random().asString(),
            null,
            null,
            null,
            InputType.TEXT,
            content,
            Map.of(),
            List.of(),
            Map.of(),
            Instant.now()
        );
    }
    
    public static class AgentRequestBuilder {
        private String id;
        private String sessionId;
        private String tenantId;
        private String userId;
        private InputType type = InputType.TEXT;
        private String content;
        private final Map<String, Object> metadata = new HashMap<>();
        private final List<Artifact> artifacts = new ArrayList<>();
        private final Map<String, Object> parameters = new HashMap<>();
        private Instant timestamp;
        
        public AgentRequestBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public AgentRequestBuilder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public AgentRequestBuilder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public AgentRequestBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public AgentRequestBuilder type(InputType type) {
            this.type = type;
            return this;
        }
        
        public AgentRequestBuilder content(String content) {
            this.content = content;
            return this;
        }
        
        public AgentRequestBuilder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public AgentRequestBuilder artifact(Artifact artifact) {
            this.artifacts.add(artifact);
            return this;
        }
        
        public AgentRequestBuilder parameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }
        
        public AgentRequestBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public AgentRequest build() {
            if (id == null) {
                id = Id.random().asString();
            }
            if (timestamp == null) {
                timestamp = Instant.now();
            }
            return new AgentRequest(id, sessionId, tenantId, userId, type, 
                content, metadata, artifacts, parameters, timestamp);
        }
    }
}