package tech.kayys.wayang.audit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;



/**
 * Audit Event
 */
public record AuditEvent(
    String id,
    String action,
    Principal principal,
    String targetType,
    String targetId,
    String targetName,
    AuditResult result,
    String details,
    Map<String, Object> attributes,
    String ipAddress,
    String userAgent,
    Instant timestamp
) {
    public static AuditEventBuilder builder() {
        return new AuditEventBuilder();
    }
    
    public static class AuditEventBuilder {
        private String id;
        private String action;
        private Principal principal;
        private String targetType;
        private String targetId;
        private String targetName;
        private AuditResult result = AuditResult.SUCCESS;
        private String details;
        private final Map<String, Object> attributes = new HashMap<>();
        private String ipAddress;
        private String userAgent;
        private Instant timestamp;
        
        public AuditEventBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public AuditEventBuilder action(String action) {
            this.action = action;
            return this;
        }
        
        public AuditEventBuilder principal(Principal principal) {
            this.principal = principal;
            return this;
        }
        
        public AuditEventBuilder targetType(String targetType) {
            this.targetType = targetType;
            return this;
        }
        
        public AuditEventBuilder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }
        
        public AuditEventBuilder targetName(String targetName) {
            this.targetName = targetName;
            return this;
        }
        
        public AuditEventBuilder result(AuditResult result) {
            this.result = result;
            return this;
        }
        
        public AuditEventBuilder details(String details) {
            this.details = details;
            return this;
        }
        
        public AuditEventBuilder attribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }
        
        public AuditEventBuilder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public AuditEventBuilder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }
        
        public AuditEventBuilder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public AuditEvent build() {
            if (id == null) {
                id = Id.random().asString();
            }
            if (timestamp == null) {
                timestamp = Instant.now();
            }
            return new AuditEvent(id, action, principal, targetType, targetId, 
                targetName, result, details, attributes, ipAddress, userAgent, timestamp);
        }
    }
}
