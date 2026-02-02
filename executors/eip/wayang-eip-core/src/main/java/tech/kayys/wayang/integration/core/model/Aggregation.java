package tech.kayys.wayang.integration.core.model;

import java.time.Instant;
import java.util.List;

public record Aggregation(String correlationId, List<Object> messages, Instant startedAt,
        Instant expiresAt, int expectedCount) {
    public boolean isComplete() {
        if (expectedCount > 0 && messages.size() >= expectedCount) {
            return true;
        }
        return Instant.now().isAfter(expiresAt);
    }
}
