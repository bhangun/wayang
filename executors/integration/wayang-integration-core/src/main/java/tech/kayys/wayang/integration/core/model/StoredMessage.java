package tech.kayys.wayang.integration.core.model;

import java.time.Instant;

public record StoredMessage(String id, Object message, Instant storedAt, Instant expiresAt) {
}
