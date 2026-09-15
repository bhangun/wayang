package tech.kayys.wayang.security.identity;

import java.util.Map;
import java.util.Objects;

/**
 * Immutable representation of the caller identity inside the Wayang security model.
 * Does not carry JWT tokens, Keycloak references, or protocol-specific details.
 */
public record Principal(
        String id,
        String name,
        IdentityType type,
        Map<String, Object> attributes
) {

    public Principal {
        Objects.requireNonNull(id,   "id");
        Objects.requireNonNull(type, "type");

        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static Principal anonymous() {
        return new Principal("anonymous", "anonymous", IdentityType.ANONYMOUS, Map.of());
    }

    public static Principal system() {
        return new Principal("system", "system", IdentityType.SYSTEM, Map.of());
    }

    public static Principal agent(String id, String name) {
        return new Principal(id, name, IdentityType.AGENT, Map.of());
    }
}
