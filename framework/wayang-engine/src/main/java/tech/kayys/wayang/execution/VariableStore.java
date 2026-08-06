package tech.kayys.wayang.execution;

import java.util.Optional;
import java.util.Set;

/**
 * Strongly-typed variable store for execution state.
 * 
 * <p>
 * Uses type-safe keys to avoid Map<String, Object> type issues.
 * Variables can be scoped (global, session, node-local).
 */
public interface VariableStore {

    /**
     * Gets a variable of the specified type.
     *
     * @param key The variable key
     * @param <T> The variable type
     * @return The variable value if present
     */
    <T> Optional<T> get(VariableKey<T> key);

    /**
     * Gets a variable with a default value.
     */
    <T> T get(VariableKey<T> key, T defaultValue);

    /**
     * Puts a variable value.
     */
    <T> void put(VariableKey<T> key, T value);

    /**
     * Puts with scope.
     */
    <T> void put(VariableKey<T> key, T value, VariableScope scope);

    /**
     * Checks if a variable exists.
     */
    boolean has(VariableKey<?> key);

    /**
     * Removes a variable.
     */
    <T> Optional<T> remove(VariableKey<T> key);

    /**
     * Returns all variable keys.
     */
    Set<VariableKey<?>> keys();

    /**
     * Returns all variables with scope.
     */
    Set<VariableEntry<?>> entries();

    /**
     * Creates a child variable store with isolated scope.
     */
    VariableStore createChild();

    /**
     * Merges variables from another store.
     */
    void merge(VariableStore other);

    /**
     * Clears variables of a specific scope.
     */
    void clear(VariableScope scope);

    /**
     * Creates a snapshot of the current variable state.
     */
    VariableSnapshot snapshot();
}
