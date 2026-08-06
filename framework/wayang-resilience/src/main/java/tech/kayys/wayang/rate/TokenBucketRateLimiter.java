package tech.kayys.wayang.rate;


/**
 * Token Bucket Rate Limiter
 */
public class TokenBucketRateLimiter implements RateLimiter {
    
    private final double rate; // tokens per second
    private final int capacity; // max tokens
    private double tokens;
    private long lastRefillTime;
    private final AtomicLong acquired = new AtomicLong(0);
    private final AtomicLong rejected = new AtomicLong(0);
    private final Object lock = new Object();
    
    public TokenBucketRateLimiter(double rate, int capacity) {
        this.rate = rate;
        this.capacity = capacity;
        this.tokens = capacity;
        this.lastRefillTime = System.nanoTime();
    }
    
    private void refill() {
        long now = System.nanoTime();
        double elapsed = (now - lastRefillTime) / 1_000_000_000.0;
        double newTokens = elapsed * rate;
        tokens = Math.min(capacity, tokens + newTokens);
        lastRefillTime = now;
    }
    
    @Override
    public boolean tryAcquire() {
        return tryAcquire(1);
    }
    
    @Override
    public boolean tryAcquire(int permits) {
        synchronized (lock) {
            refill();
            if (tokens >= permits) {
                tokens -= permits;
                acquired.incrementAndGet();
                return true;
            }
            rejected.incrementAndGet();
            return false;
        }
    }
    
    @Override
    public boolean tryAcquire(long timeoutMs) {
        return tryAcquire(1, timeoutMs);
    }
    
    @Override
    public boolean tryAcquire(int permits, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (true) {
            if (tryAcquire(permits)) {
                return true;
            }
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= timeoutMs) {
                return false;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
    
    @Override
    public RateLimiterStats getStats() {
        return new RateLimiterStats(
            acquired.get(),
            rejected.get(),
            tokens,
            capacity,
            rate,
            System.currentTimeMillis()
        );
    }
    
    @Override
    public void reset() {
        synchronized (lock) {
            tokens = capacity;
            acquired.set(0);
            rejected.set(0);
            lastRefillTime = System.nanoTime();
        }
    }
}
