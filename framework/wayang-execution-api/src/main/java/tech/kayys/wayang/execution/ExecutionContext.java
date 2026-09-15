package tech.kayys.wayang.execution;

import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;

/**
 * Contextual information associated with a running agent invocation.
 *
 * <p>Provides access to metadata, cancellation, security snapshot, and lifecycle state.
 */
public interface ExecutionContext {

    /** Immutable metadata for this execution (IDs, timestamps). */
    ExecutionMetadata metadata();

    /** The cancellation token for this execution. */
    CancellationToken cancellation();

    /** Security context snapshot active during this execution. */
    SecurityContextSnapshot security();

    /** The current lifecycle state of this execution. */
    ExecutionLifecycle lifecycle();

    /**
     * Transitions the lifecycle to the given state.
     * Only valid state transitions are permitted; implementations should throw
     * {@link IllegalStateException} for invalid transitions.
     */
    void transition(ExecutionLifecycle next);
}
