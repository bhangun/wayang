package tech.kayys.wayang.execution;

/**
 * A token that signals cancellation to a running execution.
 *
 * <p>Implementations must be thread-safe.
 */
public interface CancellationToken {

    /** Returns true if cancellation has been requested. */
    boolean isCancelled();

    /**
     * Requests cancellation of the associated execution.
     * Idempotent — multiple calls have no additional effect.
     */
    void cancel();

    /**
     * Registers a callback that will be invoked when (or if) this token is cancelled.
     * If already cancelled, the callback is invoked immediately.
     *
     * @param callback the action to run on cancellation
     */
    void onCancel(Runnable callback);

    /** Returns a token that is already in the cancelled state. */
    static CancellationToken cancelled() {
        return CancelledToken.INSTANCE;
    }

    /** Returns a token that can never be cancelled. */
    static CancellationToken noop() {
        return NoopToken.INSTANCE;
    }
}
