package tech.kayys.wayang.security.obligation.ratelimit;

import tech.kayys.wayang.security.obligation.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

public final class RateLimitObligationExecutor implements ObligationExecutor {

    private final RateLimiter rateLimiter;

    public RateLimitObligationExecutor(RateLimiter rateLimiter) {
        this.rateLimiter = Objects.requireNonNull(rateLimiter, "rateLimiter");
    }

    @Override
    public ObligationType type() {
        return StandardObligations.RATE_LIMIT;
    }

    @Override
    public CompletionStage<ObligationResult> execute(Obligation obligation, ObligationContext context) {
        String key = (String) obligation.parameters().getOrDefault("key", context.securityContext().principal().id());
        int permits = (int) obligation.parameters().getOrDefault("permits", 1);

        return rateLimiter.acquire(new RateLimitRequest(key, permits)).thenApply(decision -> {
            if (decision.allowed()) {
                return ObligationResult.success(Map.of("rate_limit_remaining", decision.remainingPermits()));
            } else {
                return ObligationResult.failure(decision.reason());
            }
        });
    }
}
