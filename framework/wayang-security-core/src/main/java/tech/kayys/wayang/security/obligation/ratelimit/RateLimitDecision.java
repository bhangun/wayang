package tech.kayys.wayang.security.obligation.ratelimit;

public record RateLimitDecision(boolean allowed, int remainingPermits, String reason) {
    public static RateLimitDecision allow(int remaining) {
        return new RateLimitDecision(true, remaining, null);
    }
    public static RateLimitDecision reject(String reason) {
        return new RateLimitDecision(false, 0, reason);
    }
}
