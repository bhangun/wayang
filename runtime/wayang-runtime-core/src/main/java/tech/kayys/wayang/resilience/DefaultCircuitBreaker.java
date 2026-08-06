package tech.kayys.wayang.resilience;


/**
 * Default Circuit Breaker Implementation
 */
public class DefaultCircuitBreaker implements CircuitBreaker {
    
    private final String name;
    private final CircuitBreakerConfig config;
    private volatile CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private final AtomicInteger failureCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private volatile long lastFailureTime = 0;
    private final AtomicInteger currentRequests = new AtomicInteger(0);
    
    public DefaultCircuitBreaker(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
    }
    
    @Override
    public String name() { return name; }
    
    @Override
    public CircuitBreakerState state() { return state; }
    
    @Override
    public boolean isOpen() { return state == CircuitBreakerState.OPEN; }
    
    @Override
    public boolean isClosed() { return state == CircuitBreakerState.CLOSED; }
    
    @Override
    public boolean isHalfOpen() { return state == CircuitBreakerState.HALF_OPEN; }
    
    @Override
    public CircuitBreakerConfig config() { return config; }
    
    @Override
    public <T> T execute(Callable<T> callable) throws Exception {
        // Check if circuit is open
        if (state == CircuitBreakerState.OPEN) {
            if (System.currentTimeMillis() - lastFailureTime > config.waitDurationMs()) {
                transitionToHalfOpen();
            } else {
                throw new CircuitBreakerOpenException("Circuit breaker '" + name + "' is OPEN");
            }
        }
        
        // Check concurrency limit
        if (currentRequests.incrementAndGet() > config.maxConcurrentRequests()) {
            currentRequests.decrementAndGet();
            throw new CircuitBreakerOpenException("Max concurrent requests exceeded");
        }
        
        try {
            T result = callable.call();
            recordSuccess();
            return result;
        } catch (Exception e) {
            recordFailure();
            throw e;
        } finally {
            currentRequests.decrementAndGet();
        }
    }
    
    @Override
    public synchronized void recordSuccess() {
        if (state == CircuitBreakerState.HALF_OPEN) {
            int successes = successCount.incrementAndGet();
            if (successes >= config.successThreshold()) {
                transitionToClosed();
            }
        } else if (state == CircuitBreakerState.CLOSED) {
            failureCount.set(0);
        }
    }
    
    @Override
    public synchronized void recordFailure() {
        lastFailureTime = System.currentTimeMillis();
        
        if (state == CircuitBreakerState.CLOSED) {
            int failures = failureCount.incrementAndGet();
            if (failures >= config.failureThreshold()) {
                transitionToOpen();
            }
        } else if (state == CircuitBreakerState.HALF_OPEN) {
            transitionToOpen();
        }
    }
    
    @Override
    public void reset() {
        state = CircuitBreakerState.CLOSED;
        failureCount.set(0);
        successCount.set(0);
        currentRequests.set(0);
    }
    
    private synchronized void transitionToOpen() {
        state = CircuitBreakerState.OPEN;
        successCount.set(0);
    }
    
    private synchronized void transitionToHalfOpen() {
        state = CircuitBreakerState.HALF_OPEN;
        successCount.set(0);
    }
    
    private synchronized void transitionToClosed() {
        state = CircuitBreakerState.CLOSED;
        failureCount.set(0);
        successCount.set(0);
    }
}