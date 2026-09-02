package tech.kayys.wayang.knowledge;

import java.time.Instant;

/**
 * Temporal validity of a knowledge item.
 */
public record KnowledgeValidity(
        Instant effectiveFrom,
        Instant effectiveUntil,
        String status
) {

    public boolean isValidAt(Instant instant) {
        Instant at = instant == null ? Instant.now() : instant;

        if (effectiveFrom != null && at.isBefore(effectiveFrom)) {
            return false;
        }

        if (effectiveUntil != null && at.isAfter(effectiveUntil)) {
            return false;
        }

        return status == null || status.isBlank() || "ACTIVE".equalsIgnoreCase(status);
    }

    public boolean isActive() {
        return isValidAt(Instant.now());
    }

    public static KnowledgeValidity active() {
        return new KnowledgeValidity(Instant.now(), null, "ACTIVE");
    }

    public static KnowledgeValidity range(Instant from, Instant until) {
        return new KnowledgeValidity(from, until, "ACTIVE");
    }

    public static KnowledgeValidity superseded() {
        return new KnowledgeValidity(null, Instant.now(), "SUPERSEDED");
    }
}
