package tech.kayys.wayang.execution;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.agent.AgentRequest;

/**
 * Minimal {@link ExecutionContext} adapter built from an {@link AgentRequest}.
 *
 * <p>Used by {@link tech.kayys.wayang.core.DefaultWayangRuntime} to convert the
 * top-level runtime request into the graph-execution context expected by
 * {@link ExecutionEngine}. Only the execution ID and variable map are populated;
 * graph-level concepts (nodes, graphs) return {@code null} since they are never
 * accessed on the linear agent execution path.
 */
public final class SimpleExecutionContext implements ExecutionContext {

    /** Type-safe key for the agent prompt string. */
    public static final VariableKey<String> PROMPT_KEY = VariableKey.of("prompt", String.class);

    private final UUID executionId;
    private final SimpleVariableStore variableStore;

    private SimpleExecutionContext(UUID executionId, Map<String, Object> vars) {
        this.executionId   = executionId;
        this.variableStore = new SimpleVariableStore(vars);
    }

    /**
     * Creates a {@link SimpleExecutionContext} from an {@link AgentRequest}.
     * The request content is stored under {@link #PROMPT_KEY}.
     */
    public static SimpleExecutionContext fromRequest(AgentRequest request) {
        UUID id = UUID.randomUUID();
        Map<String, Object> vars = new ConcurrentHashMap<>();
        if (request != null && request.content() != null) {
            vars.put(PROMPT_KEY.name(), request.content());
        }
        return new SimpleExecutionContext(id, vars);
    }

    /** Convenience accessor for the stored prompt, if any. */
    public Optional<String> prompt() {
        return variableStore.get(PROMPT_KEY);
    }

    // -------------------------------------------------------------------------
    // ExecutionContext
    // -------------------------------------------------------------------------

    @Override public UUID executionId() { return executionId; }

    /** Not used on the linear agent execution path — returns null. */
    @Override public ExecutionGraph graph() { return null; }

    @Override public VariableStore variables() { return variableStore; }

    /** Returns an empty ResourceContext. */
    @Override public ResourceContext resources() {
        return new ResourceContext(Map.of(), Map.of());
    }

    /** Not used on the linear agent execution path — returns null. */
    @Override public ExecutionNode currentNode() { return null; }

    @Override public Optional<ExecutionContext> parent() { return Optional.empty(); }

    @Override public ExecutionMetadata metadata() {
        return ExecutionMetadata.empty();
    }

    @Override
    public ExecutionContext createChild(Map<String, Object> childVariables) {
        Map<String, Object> merged = new ConcurrentHashMap<>(variableStore.rawMap());
        if (childVariables != null) merged.putAll(childVariables);
        return new SimpleExecutionContext(UUID.randomUUID(), merged);
    }

    // =========================================================================
    // Minimal VariableStore backed by a ConcurrentHashMap
    // =========================================================================

    private static final class SimpleVariableStore implements VariableStore {

        private final Map<String, Object> map;

        SimpleVariableStore(Map<String, Object> map) {
            this.map = map;
        }

        Map<String, Object> rawMap() { return map; }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Optional<T> get(VariableKey<T> key) {
            Object val = map.get(key.name());
            if (val == null) return Optional.empty();
            try { return Optional.of(key.type().cast(val)); }
            catch (ClassCastException e) { return Optional.empty(); }
        }

        @Override
        public <T> T get(VariableKey<T> key, T defaultValue) {
            return this.<T>get(key).orElse(defaultValue);
        }

        @Override public <T> void put(VariableKey<T> key, T value) {
            map.put(key.name(), value);
        }

        @Override public <T> void put(VariableKey<T> key, T value, VariableScope scope) {
            put(key, value);
        }

        @Override public boolean has(VariableKey<?> key) { return map.containsKey(key.name()); }

        @Override public <T> Optional<T> remove(VariableKey<T> key) {
            Optional<T> existing = get(key);
            map.remove(key.name());
            return existing;
        }

        @Override public Set<VariableKey<?>> keys() { return Set.of(); }
        @Override public Set<VariableEntry<?>> entries() { return Set.of(); }

        @Override public VariableStore createChild() {
            return new SimpleVariableStore(new ConcurrentHashMap<>(map));
        }

        @Override public void merge(VariableStore other) {}
        @Override public void clear(VariableScope scope) { map.clear(); }

        @Override public VariableSnapshot snapshot() {
            return new VariableSnapshot(new ConcurrentHashMap<>(map));
        }
    }
}
