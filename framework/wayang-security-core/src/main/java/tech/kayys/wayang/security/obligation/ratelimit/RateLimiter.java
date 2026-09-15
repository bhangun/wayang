package tech.kayys.wayang.security.obligation.ratelimit;

import java.util.concurrent.CompletionStage;

public interface RateLimiter {
    CompletionStage<RateLimitDecision> acquire(RateLimitRequest request);
}
