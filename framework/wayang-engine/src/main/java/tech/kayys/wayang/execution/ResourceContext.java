package tech.kayys.wayang.execution;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Resource context for execution.
 */
public final class ResourceContext {

    private final String id;
    private final Map<String, Object> resources;
    private final Map<String, Object> quotas;
    private final Map<String, Object> usage;
    private final Instant createdAt;
    private final Instant updatedAt;

    public ResourceContext(Map<String, Object> resources, Map<String, Object> quotas) {
        this.id = UUID.randomUUID().toString();
        this.resources = resources != null ? Map.copyOf(resources) : Map.of();
        this.quotas = quotas != null ? Map.copyOf(quotas) : Map.of();
        this.usage = new java.util.concurrent.ConcurrentHashMap<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public Map<String, Object> getResources() {
        return resources;
    }

    public Map<String, Object> getQuotas() {
        return quotas;
    }

    public Map<String, Object> getUsage() {
        return usage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasResource(String key) {
        return resources.containsKey(key);
    }

    public <T> Optional<T> getResource(String key, Class<T> type) {
        Object value = resources.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public <T> Optional<T> getQuota(String key, Class<T> type) {
        Object value = quotas.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public <T> Optional<T> getUsage(String key, Class<T> type) {
        Object value = usage.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public void recordUsage(String key, Object value) {
        usage.put(key, value);
    }

    public void update() {
        // Update timestamp
    }

    public static ResourceContext empty() {
        return new ResourceContext(Map.of(), Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Map<String, Object> resources = Map.of();
        private Map<String, Object> quotas = Map.of();

        public Builder resources(Map<String, Object> resources) {
            this.resources = resources;
            return this;
        }

        public Builder quotas(Map<String, Object> quotas) {
            this.quotas = quotas;
            return this;
        }

        public Builder resource(String key, Object value) {
            if (this.resources == Map.of()) {
                this.resources = new java.util.HashMap<>();
            }
            this.resources.put(key, value);
            return this;
        }

        public Builder quota(String key, Object value) {
            if (this.quotas == Map.of()) {
                this.quotas = new java.util.HashMap<>();
            }
            this.quotas.put(key, value);
            return this;
        }

        public ResourceContext build() {
            return new ResourceContext(resources, quotas);
        }
    }
}
