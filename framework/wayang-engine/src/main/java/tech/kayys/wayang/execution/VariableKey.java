package tech.kayys.wayang.execution;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Type-safe key for variables.
 * 
 * @param <T> The type of the variable value
 */
public class VariableKey<T> {

    private final String name;
    private final Class<T> type;
    private final UUID scopeId;

    private VariableKey(String name, Class<T> type, UUID scopeId) {
        this.name = name;
        this.type = type;
        this.scopeId = scopeId;
    }

    public static <T> VariableKey<T> of(String name, Class<T> type) {
        return new VariableKey<>(name, type, null);
    }

    public static <T> VariableKey<T> scoped(String name, Class<T> type, UUID scopeId) {
        return new VariableKey<>(name, type, scopeId);
    }

    public String name() {
        return name;
    }

    public Class<T> type() {
        return type;
    }

    public Optional<UUID> scopeId() {
        return Optional.ofNullable(scopeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof VariableKey))
            return false;
        VariableKey<?> that = (VariableKey<?>) o;
        return name.equals(that.name) &&
                type.equals(that.type) &&
                Objects.equals(scopeId, that.scopeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, scopeId);
    }

    @Override
    public String toString() {
        return "VariableKey{" + name + ":" + type.getSimpleName() +
                (scopeId != null ? "@" + scopeId : "") + "}";
    }
}
