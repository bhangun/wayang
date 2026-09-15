package tech.kayys.wayang.execution;

/**
 * Factory for creating cancellation tokens.
 */
public interface CancellationTokenFactory {

    /** Creates a fresh, not-yet-cancelled token. */
    CancellationToken create();

    /** Creates a child token that is cancelled when the parent is cancelled. */
    CancellationToken createChild(CancellationToken parent);
}
