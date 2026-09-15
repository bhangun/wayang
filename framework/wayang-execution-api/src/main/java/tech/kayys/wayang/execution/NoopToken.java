package tech.kayys.wayang.execution;

/**
 * A {@link CancellationToken} that can never be cancelled.
 * Useful for tests and single-shot executions that don't need cancellation.
 */
final class NoopToken implements CancellationToken {

    static final NoopToken INSTANCE = new NoopToken();

    private NoopToken() {}

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void cancel() {
        // noop — this token cannot be cancelled
    }

    @Override
    public void onCancel(Runnable callback) {
        // noop — callback never fired
    }
}
