package tech.kayys.wayang.security.authz;

import tech.kayys.wayang.security.context.SecurityContext;

import java.util.Map;
import java.util.Objects;

/**
 * Authorization request expressed in Wayang semantics, not HTTP/protocol semantics.
 * The capability, action, and resource describe what the caller wants to do.
 */
public record AuthorizationRequest(
        SecurityContext securityContext,
        String capability,
        String action,
        String resource,
        Map<String, Object> attributes
) {

    public AuthorizationRequest {
        Objects.requireNonNull(securityContext, "securityContext");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static AuthorizationRequest of(
            SecurityContext securityContext,
            String capability,
            String action,
            String resource
    ) {
        return new AuthorizationRequest(securityContext, capability, action, resource, Map.of());
    }

    public static AuthorizationRequest execute(SecurityContext securityContext, String capability) {
        return of(securityContext, capability, "execute", null);
    }
}
