package tech.kayys.wayang.spi.capability;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Execution-scoped context passed when invoking a capability.
 */
public final class CapabilityInvocationContext {

    private final String capabilityId;
    private final String providerId;
    private final String tenantId;
    private final String userId;
    private final String executionId;
    private final String sessionId;
    private final Map<String, Object> attributes;

    private CapabilityInvocationContext(Builder builder) {
        this.capabilityId = Objects.requireNonNull(builder.capabilityId, "capabilityId cannot be null");
        this.providerId = Objects.requireNonNull(builder.providerId, "providerId cannot be null");
        this.tenantId = builder.tenantId;
        this.userId = builder.userId;
        this.executionId = builder.executionId;
        this.sessionId = builder.sessionId;
        this.attributes = Map.copyOf(builder.attributes);
    }

    public String capabilityId() {
        return capabilityId;
    }

    public String providerId() {
        return providerId;
    }

    public Optional<String> tenantId() {
        return Optional.ofNullable(tenantId);
    }

    public Optional<String> userId() {
        return Optional.ofNullable(userId);
    }

    public Optional<String> executionId() {
        return Optional.ofNullable(executionId);
    }

    public Optional<String> sessionId() {
        return Optional.ofNullable(sessionId);
    }

    public Map<String, Object> attributes() {
        return attributes;
    }

    public Optional<Object> attribute(String key) {
        return Optional.ofNullable(attributes.get(key));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> attribute(String key, Class<T> type) {
        Object val = attributes.get(key);
        if (val != null && type.isInstance(val)) {
            return Optional.of((T) val);
        }
        return Optional.empty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String capabilityId;
        private String providerId;
        private String tenantId;
        private String userId;
        private String executionId;
        private String sessionId;
        private final Map<String, Object> attributes = new LinkedHashMap<>();

        public Builder capabilityId(String capabilityId) {
            this.capabilityId = capabilityId;
            return this;
        }

        public Builder providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder attribute(String key, Object value) {
            Objects.requireNonNull(key, "key cannot be null");
            if (value != null) {
                attributes.put(key, value);
            } else {
                attributes.remove(key);
            }
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public CapabilityInvocationContext build() {
            return new CapabilityInvocationContext(this);
        }
    }
}
