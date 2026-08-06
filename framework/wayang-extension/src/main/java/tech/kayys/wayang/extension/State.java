package tech.kayys.wayang.core;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the state of a resource or execution.
 * Immutable and thread-safe.
 */
public record State(
    String value,
    Set<String> validTransitions
) implements Comparable<State> {
    
    public static final State INITIAL = new State("INITIAL", Set.of("READY", "STARTED"));
    public static final State READY = new State("READY", Set.of("RUNNING"));
    public static final State RUNNING = new State("RUNNING", Set.of("PAUSED", "COMPLETED", "FAILED", "CANCELLED"));
    public static final State PAUSED = new State("PAUSED", Set.of("RUNNING", "CANCELLED"));
    public static final State COMPLETED = new State("COMPLETED", Set.of());
    public static final State FAILED = new State("FAILED", Set.of("RETRYING"));
    public static final State RETRYING = new State("RETRYING", Set.of("RUNNING", "FAILED"));
    public static final State CANCELLED = new State("CANCELLED", Set.of());
    public static final State ARCHIVED = new State("ARCHIVED", Set.of());
    public static final State DRAFT = new State("DRAFT", Set.of("READY", "ARCHIVED"));
    
    public State {
        validTransitions = validTransitions != null 
            ? Collections.unmodifiableSet(new HashSet<>(validTransitions)) 
            : Collections.emptySet();
    }
    
    public static State of(String value) {
        return new State(value, Collections.emptySet());
    }
    
    public static State of(String value, Set<String> validTransitions) {
        return new State(value, validTransitions);
    }
    
    public boolean canTransitionTo(State target) {
        return validTransitions.contains(target.value());
    }
    
    public boolean isTerminal() {
        return validTransitions.isEmpty() && !value().equals("PAUSED");
    }
    
    public boolean isActive() {
        return Set.of("READY", "RUNNING", "PAUSED", "RETRYING").contains(value);
    }
    
    @Override
    public int compareTo(State other) {
        return value.compareTo(other.value);
    }
}