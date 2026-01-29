package tech.kayys.wayang.integration.core.model;

import java.time.Instant;

public record IdempotencyRecord(String key, Instant firstSeen, Instant expiresAt) {
}
