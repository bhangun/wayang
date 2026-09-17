package tech.kayys.wayang.plugin;

import tech.kayys.wayang.spi.capability.event.CapabilityEvent;
import tech.kayys.wayang.spi.capability.event.CapabilityEventListener;
import tech.kayys.wayang.spi.capability.event.CapabilityEventPublisher;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

public final class DefaultCapabilityEventPublisher implements CapabilityEventPublisher {

    private final List<CapabilityEventListener> listeners = new CopyOnWriteArrayList<>();
    private final Executor asyncExecutor;

    public DefaultCapabilityEventPublisher() {
        this(null);
    }

    public DefaultCapabilityEventPublisher(Executor asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public void addListener(CapabilityEventListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        listeners.add(listener);
    }

    @Override
    public void removeListener(CapabilityEventListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    @Override
    public void publish(CapabilityEvent event) {
        Objects.requireNonNull(event, "event cannot be null");

        for (CapabilityEventListener listener : listeners) {
            if (asyncExecutor != null) {
                asyncExecutor.execute(() -> notifySafely(listener, event));
            } else {
                notifySafely(listener, event);
            }
        }
    }

    private void notifySafely(CapabilityEventListener listener, CapabilityEvent event) {
        try {
            listener.onEvent(event);
        } catch (Throwable t) {
            // Fault isolation: do not let listener exceptions disrupt runtime flow
        }
    }
}
