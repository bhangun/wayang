package tech.kayys.wayang.execution;

/**
 * A {@link CancellationToken} that is permanently cancelled.
 */
final class CancelledToken implements CancellationToken {

    static final CancelledToken INSTANCE = new CancelledToken();

    private CancelledToken() {}

    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public void cancel() {
        // already cancelled
    }

    @Override
    public void onCancel(Runnable callback) {
        callback.run();
    }
}
