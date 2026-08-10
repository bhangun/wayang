package tech.kayys.wayang.resilience;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of the CircuitBreaker interface.
 */
public class DefaultCircuitBreaker implements CircuitBreaker {

    private final String name;
    private final CircuitBreakerConfig config;
    private volatile CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile long stateChangedAt = System.currentTimeMillis();

    public DefaultCircuitBreaker(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public CircuitBreakerState state() {
        maybeTransitionFromOpen();
        return state;
    }

    @Override
    public boolean isOpen() {
        return state() == CircuitBreakerState.OPEN;
    }

    @Override
    public boolean isClosed() {
        return state() == CircuitBreakerState.CLOSED;
    }

    @Override
    public boolean isHalfOpen() {
        return state() == CircuitBreakerState.HALF_OPEN;
    }

    @Override
    public <T> T execute(Callable<T> callable) throws Exception {
        if (isOpen()) {
            throw new CircuitBreakerOpenException("Circuit breaker '" + name + "' is OPEN");
        }
        try {
            T result = callable.call();
            recordSuccess();
            return result;
        } catch (Exception e) {
            recordFailure();
            throw e;
        }
    }

    @Override
    public void recordSuccess() {
        failureCount.set(0);
        if (state == CircuitBreakerState.HALF_OPEN) {
            int successes = successCount.incrementAndGet();
            if (successes >= config.successThreshold()) {
                transitionTo(CircuitBreakerState.CLOSED);
            }
        }
    }

    @Override
    public void recordFailure() {
        int failures = failureCount.incrementAndGet();
        if (failures >= config.failureThreshold()) {
            transitionTo(CircuitBreakerState.OPEN);
        }
    }

    @Override
    public void reset() {
        failureCount.set(0);
        successCount.set(0);
        transitionTo(CircuitBreakerState.CLOSED);
    }

    @Override
    public CircuitBreakerConfig config() {
        return config;
    }

    private void transitionTo(CircuitBreakerState newState) {
        this.state = newState;
        this.stateChangedAt = System.currentTimeMillis();
        if (newState == CircuitBreakerState.CLOSED || newState == CircuitBreakerState.OPEN) {
            successCount.set(0);
        }
    }

    private void maybeTransitionFromOpen() {
        if (state == CircuitBreakerState.OPEN) {
            long elapsed = System.currentTimeMillis() - stateChangedAt;
            if (elapsed >= config.waitDurationMs()) {
                transitionTo(CircuitBreakerState.HALF_OPEN);
            }
        }
    }
}
