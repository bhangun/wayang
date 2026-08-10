package tech.kayys.wayang.resilience;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Retry Template
 */
public class RetryTemplate {
    
    private final RetryPolicy policy;
    private final CircuitBreaker circuitBreaker;
    private final Map<String, Object> context = new ConcurrentHashMap<>();
    
    public RetryTemplate() {
        this(DefaultRetryPolicy.defaults());
    }
    
    public RetryTemplate(RetryPolicy policy) {
        this(policy, null);
    }
    
    public RetryTemplate(RetryPolicy policy, CircuitBreaker circuitBreaker) {
        this.policy = policy;
        this.circuitBreaker = circuitBreaker;
    }
    
    public <T> T execute(Callable<T> callable) throws Exception {
        int attempt = 0;
        while (true) {
            try {
                if (circuitBreaker != null) {
                    return circuitBreaker.execute(callable);
                } else {
                    return callable.call();
                }
            } catch (Exception e) {
                if (!policy.shouldRetry(attempt, e)) {
                    throw e;
                }
                attempt++;
                long waitMs = policy.waitDurationMs(attempt);
                if (waitMs > 0) {
                    Thread.sleep(waitMs);
                }
            }
        }
    }
    
    public <T> CompletableFuture<T> executeAsync(Callable<T> callable) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return execute(callable);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
    
    public void setContext(String key, Object value) {
        context.put(key, value);
    }
    
    public Object getContext(String key) {
        return context.get(key);
    }
}