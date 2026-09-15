package tech.kayys.wayang.security.obligation.ratelimit;

public record RateLimitRequest(String key, int permits) {
    public static RateLimitRequest one(String key) {
        return new RateLimitRequest(key, 1);
    }
}
