package tech.kayys.wayang.execution;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Snapshot of variable state.
 */
public final class VariableSnapshot {

    private final String id;
    private final Instant timestamp;
    private final java.util.Map<String, Object> values;

    public VariableSnapshot(java.util.Map<String, Object> values) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = Instant.now();
        this.values = values != null ? java.util.Map.copyOf(values) : java.util.Map.of();
    }

    public String getId() {
        return id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public java.util.Map<String, Object> getValues() {
        return values;
    }

    public <T> Optional<T> getValue(String key, Class<T> type) {
        Object value = values.get(key);
        if (value != null && type.isInstance(value)) {
            return Optional.of(type.cast(value));
        }
        return Optional.empty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private java.util.Map<String, Object> values;

        public Builder values(java.util.Map<String, Object> values) {
            this.values = values;
            return this;
        }

        public VariableSnapshot build() {
            return new VariableSnapshot(values);
        }
    }
}
