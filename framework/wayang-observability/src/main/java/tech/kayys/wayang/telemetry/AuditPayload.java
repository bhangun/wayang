package tech.kayys.wayang.telemetry;

import java.time.Instant;
import java.util.Map;

/**
 * Audit payload for provenance tracking.
 * Replaces the removed tech.kayys.gollek.spi.observability.AuditPayload dependency.
 */
public record AuditPayload(
        String eventType,
        String entityId,
        String entityType,
        String actorId,
        Instant timestamp,
        Map<String, Object> attributes
) {
    public static AuditPayload of(String eventType, String entityId, String entityType, String actorId) {
        return new AuditPayload(eventType, entityId, entityType, actorId, Instant.now(), Map.of());
    }

    public static AuditPayload of(String eventType, String entityId, String entityType,
                                   String actorId, Map<String, Object> attributes) {
        return new AuditPayload(eventType, entityId, entityType, actorId, Instant.now(), attributes);
    }
}
