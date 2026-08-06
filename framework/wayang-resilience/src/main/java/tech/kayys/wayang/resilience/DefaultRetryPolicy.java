package tech.kayys.wayang.resilience;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;

public record DefaultRetryPolicy(
    int maxAttempts,
    long initialDelayMs,
    long maxDelayMs,
    double backoffMultiplier,
    List<Class<? extends Exception>> retryableExceptions
) implements RetryPolicy {
    
    public static DefaultRetryPolicy defaults() {
        return new DefaultRetryPolicy(3, 1000, 30000, 2.0, List.of(Exception.class));
    }
    
    @Override
    public boolean shouldRetry(int attempt, Exception error) {
        if (attempt >= maxAttempts) return false;
        if (retryableExceptions == null || retryableExceptions.isEmpty()) return true;
        return retryableExceptions.stream().anyMatch(e -> e.isInstance(error));
    }
    
    @Override
    public long waitDurationMs(int attempt) {
        long delay = (long) (initialDelayMs * Math.pow(backoffMultiplier, attempt));
        return Math.min(delay, maxDelayMs);
    }
}
