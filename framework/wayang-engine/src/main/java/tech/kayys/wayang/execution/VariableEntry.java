package tech.kayys.wayang.execution;

import java.time.Instant;

/**
 * Entry for a variable in the variable store.
 */
public final class VariableEntry<T> {

    private final VariableKey<T> key;
    private final T value;
    private final VariableScope scope;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final String createdBy;

    public VariableEntry(VariableKey<T> key, T value, VariableScope scope, String createdBy) {
        this.key = key;
        this.value = value;
        this.scope = scope != null ? scope : VariableScope.SESSION;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
        this.createdBy = createdBy;
    }

    public VariableKey<T> getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }

    public VariableScope getScope() {
        return scope;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public VariableEntry<T> withValue(T newValue) {
        return new VariableEntry<>(key, newValue, scope, createdBy);
    }

    public VariableEntry<T> withScope(VariableScope newScope) {
        return new VariableEntry<>(key, value, newScope, createdBy);
    }
}
