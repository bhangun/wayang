package tech.kayys.wayang.security.obligation.ratelimit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class InMemoryRateLimiter implements RateLimiter {

    private final int maxPermits;
    private final ConcurrentMap<String, AtomicInteger> counters = new ConcurrentHashMap<>();

    public InMemoryRateLimiter(int maxPermits) {
        this.maxPermits = maxPermits;
    }

    @Override
    public CompletionStage<RateLimitDecision> acquire(RateLimitRequest request) {
        AtomicInteger counter = counters.computeIfAbsent(request.key(), k -> new AtomicInteger(0));
        int current = counter.addAndGet(request.permits());
        if (current <= maxPermits) {
            return CompletableFuture.completedFuture(RateLimitDecision.allow(maxPermits - current));
        } else {
            return CompletableFuture.completedFuture(RateLimitDecision.reject("Rate limit exceeded for key: " + request.key()));
        }
    }

    public void reset() {
        counters.clear();
    }
}
