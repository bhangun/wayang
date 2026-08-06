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


import java.time.Instant;

/**
 * Audit Query
 */
public record AuditQuery(
    String userId,
    String action,
    String targetType,
    String targetId,
    AuditResult result,
    Instant from,
    Instant to,
    int limit,
    int offset,
    String sortBy,
    boolean ascending
) {
    public static AuditQueryBuilder builder() {
        return new AuditQueryBuilder();
    }
    
    public static class AuditQueryBuilder {
        private String userId;
        private String action;
        private String targetType;
        private String targetId;
        private AuditResult result;
        private Instant from;
        private Instant to;
        private int limit = 100;
        private int offset = 0;
        private String sortBy = "timestamp";
        private boolean ascending = false;
        
        public AuditQueryBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public AuditQueryBuilder action(String action) {
            this.action = action;
            return this;
        }
        
        public AuditQueryBuilder targetType(String targetType) {
            this.targetType = targetType;
            return this;
        }
        
        public AuditQueryBuilder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }
        
        public AuditQueryBuilder result(AuditResult result) {
            this.result = result;
            return this;
        }
        
        public AuditQueryBuilder from(Instant from) {
            this.from = from;
            return this;
        }
        
        public AuditQueryBuilder to(Instant to) {
            this.to = to;
            return this;
        }
        
        public AuditQueryBuilder limit(int limit) {
            this.limit = limit;
            return this;
        }
        
        public AuditQueryBuilder offset(int offset) {
            this.offset = offset;
            return this;
        }
        
        public AuditQueryBuilder sortBy(String sortBy) {
            this.sortBy = sortBy;
            return this;
        }
        
        public AuditQueryBuilder ascending(boolean ascending) {
            this.ascending = ascending;
            return this;
        }
        
        public AuditQuery build() {
            return new AuditQuery(userId, action, targetType, targetId, result, 
                from, to, limit, offset, sortBy, ascending);
        }
    }
}