package tech.kayys.wayang.core.model;


/**
 * Rate limit status
 */

record RateLimitStatus(
    int remaining,
    int limit,
    long resetTimeEpochSeconds
) {}