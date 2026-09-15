package tech.kayys.wayang.security.identity;

import java.util.Map;

/**
 * Raw, verified identity as produced by an authentication mechanism (e.g. Keycloak JWT, DID/WBA).
 * Adapters map this into a {@link Principal} for Wayang runtime use.
 */
public record Identity(
        String subject,
        String issuer,
        IdentityType type,
        Map<String, Object> claims
) {

    public Identity {
        claims = claims == null ? Map.of() : Map.copyOf(claims);
    }
}
