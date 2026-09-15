package tech.kayys.wayang.security.obligation;

import tech.kayys.wayang.security.context.SecurityContext;

import java.util.Map;
import java.util.Objects;

/**
 * Context provided to an {@link ObligationExecutor}.
 */
public record ObligationContext(
        SecurityContext securityContext,
        Map<String, Object> attributes
) {

    public ObligationContext {
        Objects.requireNonNull(securityContext, "securityContext");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static ObligationContext of(SecurityContext securityContext) {
        return new ObligationContext(securityContext, Map.of());
    }

    public static ObligationContext of(SecurityContext securityContext, Map<String, Object> attributes) {
        return new ObligationContext(securityContext, attributes);
    }
}
