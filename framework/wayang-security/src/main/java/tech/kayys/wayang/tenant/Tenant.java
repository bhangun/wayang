package tech.kayys.wayang.tenant;
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import tech.kayys.wayang.extension.Id;

/**
 * Tenant
 */
public record Tenant(
    String id,
    String name,
    String description,
    TenantStatus status,
    Map<String, Object> configuration,
    Set<String> features,
    Set<String> allowedModels,
    Long quotaLimit,
    Long usageCount,
    Instant createdAt,
    Instant updatedAt
) {
    public static TenantBuilder builder() {
        return new TenantBuilder();
    }
    
    public static class TenantBuilder {
        private String id;
        private String name;
        private String description;
        private TenantStatus status = TenantStatus.ACTIVE;
        private final Map<String, Object> configuration = new HashMap<>();
        private final Set<String> features = new HashSet<>();
        private final Set<String> allowedModels = new HashSet<>();
        private Long quotaLimit;
        private Long usageCount = 0L;
        private Instant createdAt;
        private Instant updatedAt;
        
        public TenantBuilder id(String id) {
            this.id = id;
            return this;
        }
        
        public TenantBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public TenantBuilder description(String description) {
            this.description = description;
            return this;
        }
        
        public TenantBuilder status(TenantStatus status) {
            this.status = status;
            return this;
        }
        
        public TenantBuilder config(String key, Object value) {
            this.configuration.put(key, value);
            return this;
        }
        
        public TenantBuilder feature(String feature) {
            this.features.add(feature);
            return this;
        }
        
        public TenantBuilder allowedModel(String model) {
            this.allowedModels.add(model);
            return this;
        }
        
        public TenantBuilder quotaLimit(Long quotaLimit) {
            this.quotaLimit = quotaLimit;
            return this;
        }
        
        public Tenant build() {
            if (id == null) {
                id = Id.random().asString();
            }
            if (createdAt == null) {
                createdAt = Instant.now();
            }
            if (updatedAt == null) {
                updatedAt = createdAt;
            }
            return new Tenant(
                id, name, description, status, configuration,
                features, allowedModels, quotaLimit, usageCount,
                createdAt, updatedAt
            );
        }
    }
}
