package tech.kayys.wayang.execution.core;

import tech.kayys.wayang.execution.CancellationToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe, mutable {@link CancellationToken} implementation.
 */
public final class DefaultCancellationToken implements CancellationToken {

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final List<Runnable> callbacks = new ArrayList<>();

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public synchronized void cancel() {
        if (cancelled.compareAndSet(false, true)) {
            for (Runnable callback : callbacks) {
                try {
                    callback.run();
                } catch (Exception ignored) {
                    // callbacks must not propagate exceptions
                }
            }
        }
    }

    @Override
    public synchronized void onCancel(Runnable callback) {
        if (cancelled.get()) {
            callback.run();
        } else {
            callbacks.add(callback);
        }
    }
}
