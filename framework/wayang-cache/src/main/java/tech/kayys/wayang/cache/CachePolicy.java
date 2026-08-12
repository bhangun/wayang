package tech.kayys.wayang.cache;

import java.time.Duration;

/**
 * Rules for how long cached content should survive.
 */
public record CachePolicy(
    Duration timeToLive,
    boolean persistent
) {
    public static CachePolicy temporary(Duration ttl) {
        return new CachePolicy(ttl, false);
    }
    
    public static CachePolicy persistent(Duration ttl) {
        return new CachePolicy(ttl, true);
    }
}
