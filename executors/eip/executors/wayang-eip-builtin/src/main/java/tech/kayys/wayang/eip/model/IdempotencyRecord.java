package tech.kayys.wayang.eip.model;

import java.time.Instant;

public record IdempotencyRecord(String key, Instant firstSeen, Instant expiresAt) {
}
