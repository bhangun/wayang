package tech.kayys.wayang.agent;

import tech.kayys.wayang.context.ContextData;
import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;

import tech.kayys.wayang.extension.Version;

import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.identity.ResourceId.ExecutionId;

import tech.kayys.wayang.resource.Artifact;
import tech.kayys.wayang.resource.ArtifactType;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.ResourceType;
import tech.kayys.wayang.spi.output.OutputResult;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * AgentContext - The universal context for agent execution.
 * 
 * <p>AgentContext is the primary data container that flows through the entire
 * agent lifecycle. It carries all state, variables, and artifacts between
 * phases of agent execution.</p>
 * 
 * <h2>Lifecycle Flow</h2>
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                      AgentContext                              │
 * │  ┌─────────────────────────────────────────────────────────┐  │
 * │  │ Trigger → Input → Context → Planning → Reasoning       │  │
 * │  │          → Model → Tools → Memory → Output             │  │
 * │  └─────────────────────────────────────────────────────────┘  │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h2>Key Features</h2>
 * <ul>
 *   <li><b>Immutable by default</b> - Every modification creates a new instance</li>
 *   <li><b>Thread-safe</b> - Can be shared across threads safely</li>
 *   <li><b>Versioned</b> - Tracks state changes for audit and replay</li>
 *   <li><b>Type-safe</b> - Strongly typed accessors for variables</li>
 *   <li><b>Observable</b> - Emits events for every state change</li>
 * </ul>
 * 
 * <h2>Usage Example</h2>
 * <pre>
 * {@code
 * // Create a new context
 * AgentContext ctx = AgentContext.builder()
 *     .sessionId("session-123")
 *     .tenantId("tenant-456")
 *     .principal(Principal.system())
 *     .variable("query", "What is the capital of France?")
 *     .build();
 * 
 * // Access variables with type safety
 * String query = ctx.getVariable("query", String.class);
 * 
 * // Add artifacts
 * ctx = ctx.withArtifact(TextArtifact.of("Processing..."));
 * 
 * // Add execution results
 * ctx = ctx.withResult(AgentResponse.success("Paris is the capital of France."));
 * }
 * </pre>
 * 
 * @see AgentRequest
 * @see AgentResponse
 * @see ExecutionContext
 * @see io.wayang.engine.AgentPipeline
 */
public final class AgentContext implements Resource {
    
    // ============================================================================
    // Core Fields
    // ============================================================================
    
    private final ResourceId id;
    private final String sessionId;
    private final String tenantId;
    private final String namespace;
        private final AgentRequest request;
    private final AgentResponse response;
    private final Map<String, Object> variables;
    private final Map<String, Object> attributes;
    private final List<Artifact> artifacts;

    private final ContextData contextData;

    private final OutputResult outputResult;
    private final AgentContextState state;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final int version;
    private final Map<String, String> metadata;
    private final AgentContextPhase currentPhase;
    private final long startTime;
    private final long elapsedTime;
    private final Map<String, Long> phaseTimings;
    private final Set<String> completedPhases;
    private final List<AgentContext> history;
    
    // ============================================================================
    // Private Constructor
    // ============================================================================
    
    private AgentContext(Builder builder) {
        this.id = builder.id != null ? builder.id : new tech.kayys.wayang.identity.ResourceId.AgentId(new tech.kayys.wayang.extension.Id(java.util.UUID.randomUUID()));
        this.sessionId = builder.sessionId;
        this.tenantId = builder.tenantId;
        this.namespace = builder.namespace;
        this.request = builder.request;
        this.response = builder.response;
        this.contextData = builder.contextData;
        this.outputResult = builder.outputResult;
        this.variables = builder.variables != null ? new LinkedHashMap<>(builder.variables) : new LinkedHashMap<>();
        this.attributes = builder.attributes != null ? new LinkedHashMap<>(builder.attributes) : new LinkedHashMap<>();
        this.artifacts = builder.artifacts != null ? List.copyOf(builder.artifacts) : List.of();
                
                
        this.state = builder.state != null ? builder.state : AgentContextState.INITIALIZED;
        this.createdAt = builder.createdAt != null ? builder.createdAt : Instant.now();
        this.updatedAt = builder.updatedAt != null ? builder.updatedAt : this.createdAt;
        this.version = builder.version != null ? builder.version : 1;
        this.metadata = builder.metadata != null ? new LinkedHashMap<>(builder.metadata) : new LinkedHashMap<>();
        this.currentPhase = builder.currentPhase != null ? builder.currentPhase : AgentContextPhase.INIT;
        this.startTime = builder.startTime != null ? builder.startTime : System.currentTimeMillis();
        this.elapsedTime = builder.elapsedTime != null ? builder.elapsedTime : 0L;
        this.phaseTimings = builder.phaseTimings != null ? new LinkedHashMap<>(builder.phaseTimings) : new LinkedHashMap<>();
        this.completedPhases = builder.completedPhases != null ? new LinkedHashSet<>(builder.completedPhases) : new LinkedHashSet<>();
        this.history = builder.history != null ? List.copyOf(builder.history) : List.of();
    }
    
    // ============================================================================
    // Getters - Core Identity
    // ============================================================================
    
    /**
     * Gets the unique identifier for this context.
     *
     * @return The context ID
     */
    public ResourceId id() {
        return id;
    }
    
    /**
     * Gets the session ID this context belongs to.
     *
     * @return The session ID
     */
    public String sessionId() {
        return sessionId;
    }
    
    /**
     * Gets the tenant ID this context belongs to.
     *
     * @return The tenant ID
     */
    public String tenantId() {
        return tenantId;
    }
    
    /**
     * Gets the namespace this context belongs to.
     *
     * @return The namespace
     */
    public String namespace() {
        return namespace;
    }
    
    /**
     * Gets the principal (user/service) executing this context.
     *
     * @return The principal
     */
        
    /**
     * Gets the current phase of execution.
     *
     * @return The current phase
     */
    public AgentContextPhase currentPhase() {
        return currentPhase;
    }
    
    /**
     * Gets the current state of the context.
     *
     * @return The current state
     */
    public AgentContextState state() {
        return state;
    }
    
    /**
     * Gets the version number of this context.
     * Increments with each modification.
     *
     * @return The version number
     */
    public int version() {
        return version;
    }
    
    // ============================================================================
    // Getters - Data
    // ============================================================================
    
    /**
     * Gets the agent request.
     *
     * @return The request
     */
    public AgentRequest request() {
        return request;
    }
    
    /**
     * Gets the agent response.
     *
     * @return The response
     */
    public AgentResponse response() {
        return response;
    }
    
    /**
     * Gets all variables in this context.
     *
     * @return An unmodifiable map of variables
     */
    public Map<String, Object> variables() {
        return Collections.unmodifiableMap(variables);
    }
    
    /**
     * Gets all attributes in this context.
     *
     * @return An unmodifiable map of attributes
     */
    public Map<String, Object> attributes() {
        return Collections.unmodifiableMap(attributes);
    }
    
    /**
     * Gets all artifacts in this context.
     *
     * @return An unmodifiable list of artifacts
     */
    public List<Artifact> artifacts() {
        return artifacts;
    }
    
    /**
     * Gets all events in this context.
     *
     * @return An unmodifiable list of events
     */

    /**
     * Gets all errors in this context.
     *
     * @return An unmodifiable list of errors
     */

    /**
     * Gets the context data.
     *
     * @return The context data
     */
    public ContextData contextData() {
        return contextData;
    }
    
    /**
     * Gets the plan.
     *
     * @return The plan
     */

    /**
     * Gets the reasoning result.
     *
     * @return The reasoning result
     */

    /**
     * Gets the completion result from model inference.
     *
     * @return The completion result
     */

    /**
     * Gets all tool results.
     *
     * @return An unmodifiable list of tool results
     */

    /**
     * Gets all memory records.
     *
     * @return An unmodifiable list of memory records
     */

    /**
     * Gets the evaluation result.
     *
     * @return The evaluation
     */

    /**
     * Gets the guardrail result.
     *
     * @return The guardrail result
     */

    /**
     * Gets the output result.
     *
     * @return The output result
     */
    public OutputResult outputResult() {
        return outputResult;
    }
    
    // ============================================================================
    // Getters - Timing
    // ============================================================================
    
    /**
     * Gets the creation timestamp.
     *
     * @return The creation timestamp
     */
    public Instant createdAt() {
        return createdAt;
    }
    
    /**
     * Gets the last update timestamp.
     *
     * @return The last update timestamp
     */
    public Instant updatedAt() {
        return updatedAt;
    }
    
    /**
     * Gets the start time in milliseconds.
     *
     * @return The start time
     */
    public long startTime() {
        return startTime;
    }
    
    /**
     * Gets the elapsed time in milliseconds.
     *
     * @return The elapsed time
     */
    public long elapsedTime() {
        return elapsedTime;
    }
    
    /**
     * Gets the duration since creation in milliseconds.
     *
     * @return The duration
     */
    public long durationMs() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Gets the timing for a specific phase.
     *
     * @param phase The phase name
     * @return The timing in milliseconds, or null if not recorded
     */
    public Long getPhaseTiming(String phase) {
        return phaseTimings.get(phase);
    }
    
    /**
     * Gets all phase timings.
     *
     * @return An unmodifiable map of phase timings
     */
    public Map<String, Long> phaseTimings() {
        return Collections.unmodifiableMap(phaseTimings);
    }
    
    /**
     * Checks if a phase has been completed.
     *
     * @param phase The phase name
     * @return True if completed
     */
    public boolean isPhaseCompleted(String phase) {
        return completedPhases.contains(phase);
    }
    
    /**
     * Gets all completed phases.
     *
     * @return An unmodifiable set of completed phases
     */
    public Set<String> completedPhases() {
        return Collections.unmodifiableSet(completedPhases);
    }
    
    // ============================================================================
    // Getters - Metadata & History
    // ============================================================================
    
    /**
     * Gets all metadata.
     *
     * @return An unmodifiable map of metadata
     */
    
    
    /**
     * Gets a metadata value.
     *
     * @param key The metadata key
     * @return The value, or null if not found
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Gets the history of context versions.
     *
     * @return An unmodifiable list of historical contexts
     */
    public List<AgentContext> history() {
        return history;
    }
    
    /**
     * Gets the last context in history.
     *
     * @return The last context, or null if no history
     */
    public AgentContext getLastContext() {
        return history.isEmpty() ? null : history.get(history.size() - 1);
    }
    
    // ============================================================================
    // Variable Accessors
    // ============================================================================
    
    /**
     * Checks if a variable exists.
     *
     * @param key The variable key
     * @return True if exists
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }
    
    /**
     * Gets a variable as an Object.
     *
     * @param key The variable key
     * @return The value, or null if not found
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    /**
     * Gets a variable with type safety.
     *
     * @param key The variable key
     * @param type The expected type
     * @param <T> The type
     * @return The value, or null if not found or type mismatch
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, Class<T> type) {
        Object value = variables.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Gets a variable with a default value.
     *
     * @param key The variable key
     * @param defaultValue The default value
     * @param type The expected type
     * @param <T> The type
     * @return The value, or the default if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, T defaultValue, Class<T> type) {
        Object value = variables.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        return defaultValue;
    }
    
    /**
     * Gets a variable as a String.
     *
     * @param key The variable key
     * @return The value as a String, or null if not found
     */
    public String getVariableAsString(String key) {
        Object value = variables.get(key);
        return value != null ? value.toString() : null;
    }
    
    /**
     * Gets a variable as an Integer.
     *
     * @param key The variable key
     * @return The value as an Integer, or null if not found
     */
    public Integer getVariableAsInt(String key) {
        Object value = variables.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Gets a variable as a Long.
     *
     * @param key The variable key
     * @return The value as a Long, or null if not found
     */
    public Long getVariableAsLong(String key) {
        Object value = variables.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Gets a variable as a Boolean.
     *
     * @param key The variable key
     * @return The value as a Boolean, or null if not found
     */
    public Boolean getVariableAsBoolean(String key) {
        Object value = variables.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }
    
    /**
     * Gets a variable as a List.
     *
     * @param key The variable key
     * @param elementType The expected element type
     * @param <T> The element type
     * @return The value as a List, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getVariableAsList(String key, Class<T> elementType) {
        Object value = variables.get(key);
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            if (list.isEmpty() || elementType.isInstance(list.get(0))) {
                return (List<T>) list;
            }
        }
        return null;
    }
    
    /**
     * Gets a variable as a Map.
     *
     * @param key The variable key
     * @param keyType The key type
     * @param valueType The value type
     * @param <K> The key type
     * @param <V> The value type
     * @return The value as a Map, or null if not found
     */
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getVariableAsMap(String key, Class<K> keyType, Class<V> valueType) {
        Object value = variables.get(key);
        if (value instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) value;
            if (map.isEmpty() || (keyType.isInstance(map.keySet().iterator().next()) && 
                valueType.isInstance(map.values().iterator().next()))) {
                return (Map<K, V>) map;
            }
        }
        return null;
    }
    
    // ============================================================================
    // Attribute Accessors
    // ============================================================================
    
    /**
     * Checks if an attribute exists.
     *
     * @param key The attribute key
     * @return True if exists
     */
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    /**
     * Gets an attribute as an Object.
     *
     * @param key The attribute key
     * @return The value, or null if not found
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    
    /**
     * Gets an attribute with type safety.
     *
     * @param key The attribute key
     * @param type The expected type
     * @param <T> The type
     * @return The value, or null if not found or type mismatch
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value == null) {
            return null;
        }
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    // ============================================================================
    // Artifact Operations
    // ============================================================================
    
    /**
     * Finds an artifact by ID.
     *
     * @param artifactId The artifact ID
     * @return The artifact, or null if not found
     */
    public Artifact findArtifact(String artifactId) {
        for (Artifact artifact : artifacts) {
            if (artifact.id().asString().equals(artifactId)) {
                return artifact;
            }
        }
        return null;
    }
    
    /**
     * Finds artifacts by type.
     *
     * @param type The artifact type
     * @return A list of matching artifacts
     */
    public List<Artifact> findArtifactsByType(ArtifactType type) {
        List<Artifact> result = new ArrayList<>();
        for (Artifact artifact : artifacts) {
            if (artifact.type() == type) {
                result.add(artifact);
            }
        }
        return result;
    }
    
    /**
     * Gets the latest artifact of a specific type.
     *
     * @param type The artifact type
     * @return The latest artifact, or null if not found
     */
    public Artifact getLatestArtifact(ArtifactType type) {
        List<Artifact> matching = findArtifactsByType(type);
        if (matching.isEmpty()) {
            return null;
        }
        return matching.get(matching.size() - 1);
    }
    
    // ============================================================================
    // Event Operations
    // ============================================================================
    

    
    // ============================================================================
    // State Checks
    // ============================================================================
    
    /**
     * Checks if the context has completed successfully.
     *
     * @return True if completed
     */
    public boolean isCompleted() {
        return state == AgentContextState.COMPLETED;
    }
    
    /**
     * Checks if the context has failed.
     *
     * @return True if failed
     */
    public boolean isFailed() {
        return state == AgentContextState.FAILED;
    }
    
    /**
     * Checks if the context is still running.
     *
     * @return True if running
     */
    public boolean isRunning() {
        return state == AgentContextState.EXECUTING;
    }
    
    /**
     * Checks if the context is paused.
     *
     * @return True if paused
     */
    public boolean isPaused() {
        return state == AgentContextState.PAUSED;
    }
    
    /**
     * Checks if the context is in a terminal state.
     *
     * @return True if terminal
     */
    public boolean isTerminal() {
        return state == AgentContextState.COMPLETED || 
               state == AgentContextState.FAILED || 
               state == AgentContextState.CANCELLED;
    }
    


    // ============================================================================
    // Builder Methods - Create Modified Copies
    // ============================================================================
    
    /**
     * Creates a new context with an added variable.
     *
     * @param key The variable key
     * @param value The variable value
     * @return A new context instance
     */
    public AgentContext withVariable(String key, Object value) {
        Map<String, Object> newVariables = new LinkedHashMap<>(variables);
        newVariables.put(key, value);
        return new Builder(this)
            .variables(newVariables)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with multiple variables.
     *
     * @param variables The variables to add
     * @return A new context instance
     */
    public AgentContext withVariables(Map<String, Object> variables) {
        Map<String, Object> newVariables = new LinkedHashMap<>(this.variables);
        newVariables.putAll(variables);
        return new Builder(this)
            .variables(newVariables)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with a removed variable.
     *
     * @param key The variable key to remove
     * @return A new context instance
     */
    public AgentContext withoutVariable(String key) {
        Map<String, Object> newVariables = new LinkedHashMap<>(variables);
        newVariables.remove(key);
        return new Builder(this)
            .variables(newVariables)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with an added attribute.
     *
     * @param key The attribute key
     * @param value The attribute value
     * @return A new context instance
     */
    public AgentContext withAttribute(String key, Object value) {
        Map<String, Object> newAttributes = new LinkedHashMap<>(attributes);
        newAttributes.put(key, value);
        return new Builder(this)
            .attributes(newAttributes)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with multiple attributes.
     *
     * @param attributes The attributes to add
     * @return A new context instance
     */
    public AgentContext withAttributes(Map<String, Object> attributes) {
        Map<String, Object> newAttributes = new LinkedHashMap<>(this.attributes);
        newAttributes.putAll(attributes);
        return new Builder(this)
            .attributes(newAttributes)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with an added artifact.
     *
     * @param artifact The artifact to add
     * @return A new context instance
     */
    public AgentContext withArtifact(Artifact artifact) {
        List<Artifact> newArtifacts = new ArrayList<>(artifacts);
        newArtifacts.add(artifact);
        return new Builder(this)
            .artifacts(newArtifacts)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with multiple artifacts.
     *
     * @param artifacts The artifacts to add
     * @return A new context instance
     */
    public AgentContext withArtifacts(List<Artifact> artifacts) {
        List<Artifact> newArtifacts = new ArrayList<>(this.artifacts);
        newArtifacts.addAll(artifacts);
        return new Builder(this)
            .artifacts(newArtifacts)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with an added event.
     *
     * @param event The event to add
     * @return A new context instance
     */
    
    /**
     * Creates a new context with an added error.
     *
     * @param error The error to add
     * @return A new context instance
     */
    
    /**
     * Creates a new context with a new state.
     *
     * @param state The new state
     * @return A new context instance
     */
    public AgentContext withState(AgentContextState state) {
        return new Builder(this)
            .state(state)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with a new phase.
     *
     * @param phase The new phase
     * @return A new context instance
     */
    public AgentContext withPhase(AgentContextPhase phase) {
        Set<String> newCompletedPhases = new LinkedHashSet<>(completedPhases);
        if (currentPhase != null && currentPhase != AgentContextPhase.INIT) {
            newCompletedPhases.add(currentPhase.name());
        }
        
        Map<String, Long> newPhaseTimings = new LinkedHashMap<>(phaseTimings);
        if (currentPhase != null) {
            long duration = System.currentTimeMillis() - (startTime + elapsedTime);
            newPhaseTimings.put(currentPhase.name(), duration);
        }
        
        long newElapsedTime = elapsedTime + System.currentTimeMillis() - (startTime + elapsedTime);
        
        return new Builder(this)
            .currentPhase(phase)
            .completedPhases(newCompletedPhases)
            .phaseTimings(newPhaseTimings)
            .elapsedTime(newElapsedTime)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with a new request.
     *
     * @param request The new request
     * @return A new context instance
     */
    public AgentContext withRequest(AgentRequest request) {
        return new Builder(this)
            .request(request)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with a new response.
     *
     * @param response The new response
     * @return A new context instance
     */
    public AgentContext withResponse(AgentResponse response) {
        return new Builder(this)
            .response(response)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with context data.
     *
     * @param contextData The context data
     * @return A new context instance
     */
    public AgentContext withContextData(ContextData contextData) {
        return new Builder(this)
            .contextData(contextData)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }

    /**
     * Creates a new context with a reasoning result.
     *
     * @param reasoningResult The reasoning result
     * @return A new context instance
     */
    
    
    /**
     * Creates a new context with a completion result.
     *
     * @param completionResult The completion result
     * @return A new context instance
     */
    

    /**
     * Creates a new context with a guardrail result.
     *
     * @param guardrailResult The guardrail result
     * @return A new context instance
     */
    
    
    /**
     * Creates a new context with an output result.
     *
     * @param outputResult The output result
     * @return A new context instance
     */
    public AgentContext withOutputResult(OutputResult outputResult) {
        return new Builder(this)
            .outputResult(outputResult)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with metadata.
     *
     * @param key The metadata key
     * @param value The metadata value
     * @return A new context instance
     */
    public AgentContext withMetadata(String key, String value) {
        Map<String, String> newMetadata = new LinkedHashMap<>(metadata);
        newMetadata.put(key, value);
        return new Builder(this)
            .metadata(newMetadata)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    /**
     * Creates a new context with multiple metadata entries.
     *
     * @param metadata The metadata to add
     * @return A new context instance
     */
    public AgentContext withMetadata(Map<String, String> metadata) {
        Map<String, String> newMetadata = new LinkedHashMap<>(this.metadata);
        newMetadata.putAll(metadata);
        return new Builder(this)
            .metadata(newMetadata)
            .updatedAt(Instant.now())
            .version(version + 1)
            .history(addHistory())
            .build();
    }
    
    // ============================================================================
    // Utility Methods
    // ============================================================================
    
    /**
     * Creates a new context that is a complete copy of this one.
     *
     * @return A new context instance
     */
    public AgentContext copy() {
        return new Builder(this)
            .updatedAt(Instant.now())
            .version(version + 1)
            .build();
    }
    
    /**
     * Checks if the context has completed a specific phase.
     *
     * @param phase The phase name
     * @return True if completed
     */
    public boolean hasCompletedPhase(String phase) {
        return completedPhases.contains(phase);
    }
    
    /**
     * Gets the duration of a specific phase.
     *
     * @param phase The phase name
     * @return The duration in milliseconds, or null if not recorded
     */
    public Long getPhaseDuration(String phase) {
        return phaseTimings.get(phase);
    }
    
    /**
     * Gets the total duration of all phases.
     *
     * @return The total duration in milliseconds
     */
    public long getTotalPhaseDuration() {
        long total = 0;
        for (Long duration : phaseTimings.values()) {
            total += duration;
        }
        return total;
    }

    /**
     * Adds the current context to history.
     *
     * @return A new history list
     */
    private List<AgentContext> addHistory() {
        List<AgentContext> newHistory = new ArrayList<>(history);
        newHistory.add(this);
        return newHistory;
    }
    
    // ============================================================================
    // Resource Interface Implementation
    // ============================================================================
    
    
    
    @Override
    public ResourceType type() {
        return new ResourceType.Custom("agent-context");
    }
    
    @Override
    public Metadata metadata() {
        return Metadata.builder()
            .name("agent-context-" + id)
            .description("Agent execution context")
            .version(new Version(version, 0, 0))
            .label("state", state.name())
            .label("phase", currentPhase != null ? currentPhase.name() : "unknown")
            .label("sessionId", sessionId)
            .label("tenantId", tenantId)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
    
    // ============================================================================
    // Object Overrides
    // ============================================================================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AgentContext that = (AgentContext) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "AgentContext{" +
            "id='" + id + '\'' +
            ", sessionId='" + sessionId + '\'' +
            ", tenantId='" + tenantId + '\'' +
            ", state=" + state +
            ", phase=" + currentPhase +
            ", variables=" + variables.size() +
            ", artifacts=" + artifacts.size() +
            ", version=" + version +
            ", durationMs=" + durationMs() +
            '}';
    }
    
    // ============================================================================
    // Builder
    // ============================================================================
    
    /**
     * Creates a new builder for AgentContext.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Creates a new builder from an existing context.
     *
     * @param context The existing context
     * @return A new builder
     */
    public static Builder builder(AgentContext context) {
        return new Builder(context);
    }
    
    /**
     * AgentContext Builder
     */
    public static final class Builder {
        private ResourceId id;
        private String sessionId;
        private String tenantId;
        private String namespace;
                private AgentRequest request;
        private AgentResponse response;
        private Map<String, Object> variables;
        private Map<String, Object> attributes;
        private List<Artifact> artifacts;

        private ContextData contextData;

        private OutputResult outputResult;
        private AgentContextState state;
        private Instant createdAt;
        private Instant updatedAt;
        private Integer version;
        private Map<String, String> metadata;
        private AgentContextPhase currentPhase;
        private Long startTime;
        private Long elapsedTime;
        private Map<String, Long> phaseTimings;
        private Set<String> completedPhases;
        private List<AgentContext> history;
        
        public Builder() {}
        
        public Builder(AgentContext context) {
            this.id = context.id;
            this.sessionId = context.sessionId;
            this.tenantId = context.tenantId;
            this.namespace = context.namespace;
                        this.request = context.request;
            this.response = context.response;
            this.variables = new LinkedHashMap<>(context.variables);
            this.attributes = new LinkedHashMap<>(context.attributes);
            this.artifacts = new ArrayList<>(context.artifacts);
            
            
            this.contextData = context.contextData;
            
            
            
            
            
            
            
            this.outputResult = context.outputResult;
            this.state = context.state;
            this.createdAt = context.createdAt;
            this.updatedAt = context.updatedAt;
            this.version = context.version;
            this.metadata = new LinkedHashMap<>(context.metadata);
            this.currentPhase = context.currentPhase;
            this.startTime = context.startTime;
            this.elapsedTime = context.elapsedTime;
            this.phaseTimings = new LinkedHashMap<>(context.phaseTimings);
            this.completedPhases = new LinkedHashSet<>(context.completedPhases);
            this.history = new ArrayList<>(context.history);
        }
        
        public Builder id(ResourceId id) {
            this.id = id;
            return this;
        }
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder namespace(String namespace) {
            this.namespace = namespace;
            return this;
        }
        
                
        public Builder request(AgentRequest request) {
            this.request = request;
            return this;
        }
        
        public Builder response(AgentResponse response) {
            this.response = response;
            return this;
        }
        
        public Builder variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }
        
        public Builder variable(String key, Object value) {
            if (this.variables == null) {
                this.variables = new LinkedHashMap<>();
            }
            this.variables.put(key, value);
            return this;
        }
        
        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }
        
        public Builder attribute(String key, Object value) {
            if (this.attributes == null) {
                this.attributes = new LinkedHashMap<>();
            }
            this.attributes.put(key, value);
            return this;
        }
        
        public Builder artifacts(List<Artifact> artifacts) {
            this.artifacts = artifacts;
            return this;
        }
        
        public Builder artifact(Artifact artifact) {
            if (this.artifacts == null) {
                
            }
            this.artifacts.add(artifact);
            return this;
        }
        public Builder contextData(ContextData contextData) {
            this.contextData = contextData;
            return this;
        }
        public Builder outputResult(OutputResult outputResult) {
            this.outputResult = outputResult;
            return this;
        }
        
        public Builder state(AgentContextState state) {
            this.state = state;
            return this;
        }
        
        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }
        
        public Builder version(Integer version) {
            this.version = version;
            return this;
        }
        
        public Builder metadata(Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder metadata(String key, String value) {
            if (this.metadata == null) {
                this.metadata = new LinkedHashMap<>();
            }
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder currentPhase(AgentContextPhase currentPhase) {
            this.currentPhase = currentPhase;
            return this;
        }
        
        public Builder startTime(Long startTime) {
            this.startTime = startTime;
            return this;
        }
        
        public Builder elapsedTime(Long elapsedTime) {
            this.elapsedTime = elapsedTime;
            return this;
        }
        
        public Builder phaseTimings(Map<String, Long> phaseTimings) {
            this.phaseTimings = phaseTimings;
            return this;
        }
        
        public Builder phaseTiming(String phase, Long duration) {
            if (this.phaseTimings == null) {
                this.phaseTimings = new LinkedHashMap<>();
            }
            this.phaseTimings.put(phase, duration);
            return this;
        }
        
        public Builder completedPhases(Set<String> completedPhases) {
            this.completedPhases = completedPhases;
            return this;
        }
        
        public Builder completedPhase(String phase) {
            if (this.completedPhases == null) {
                this.completedPhases = new LinkedHashSet<>();
            }
            this.completedPhases.add(phase);
            return this;
        }
        
        public Builder history(List<AgentContext> history) {
            this.history = history;
            return this;
        }
        
        public Builder history(AgentContext context) {
            if (this.history == null) {
                
            }
            this.history.add(context);
            return this;
        }
        
        /**
         * Builds the AgentContext.
         *
         * @return The built AgentContext
         */
        public AgentContext build() {
            return new AgentContext(this);
        }
    }
}

