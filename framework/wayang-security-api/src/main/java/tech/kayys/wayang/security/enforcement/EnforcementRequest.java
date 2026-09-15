package tech.kayys.wayang.security.enforcement;

import tech.kayys.wayang.security.context.SecurityContext;

import java.util.Map;
import java.util.Objects;

/**
 * Request submitted to a {@link PolicyEnforcementPoint}.
 * The payload {@code data} can be any domain payload (e.g. JSON, Map, text) that may undergo transformation.
 */
public record EnforcementRequest(
        SecurityContext securityContext,
        String capability,
        String action,
        String resource,
        Object data,
        Map<String, Object> attributes
) {

    public EnforcementRequest {
        Objects.requireNonNull(securityContext, "securityContext");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static EnforcementRequest of(
            SecurityContext securityContext,
            String capability,
            String action,
            String resource,
            Object data
    ) {
        return new EnforcementRequest(securityContext, capability, action, resource, data, Map.of());
    }

    public static EnforcementRequest of(SecurityContext securityContext, String capability, Object data) {
        return of(securityContext, capability, "execute", null, data);
    }
}
