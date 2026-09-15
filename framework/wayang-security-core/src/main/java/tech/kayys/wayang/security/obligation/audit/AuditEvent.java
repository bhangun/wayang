package tech.kayys.wayang.security.obligation.audit;

import tech.kayys.wayang.security.identity.Principal;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record AuditEvent(
        Instant timestamp,
        String action,
        String capability,
        Principal principal,
        String tenantId,
        Map<String, Object> attributes
) {

    public AuditEvent {
        Objects.requireNonNull(timestamp, "timestamp");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static AuditEvent of(String action, String capability, Principal principal, String tenantId) {
        return new AuditEvent(Instant.now(), action, capability, principal, tenantId, Map.of());
    }
}
