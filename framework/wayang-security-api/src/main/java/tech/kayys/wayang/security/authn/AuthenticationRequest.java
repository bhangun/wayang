package tech.kayys.wayang.security.authn;

import java.util.Map;

/**
 * Protocol-neutral authentication request.
 * The {@code credential} contains the raw credential (e.g. Bearer token value, API key).
 * The {@code scheme} identifies the mechanism (e.g. "Bearer", "ApiKey", "DID").
 */
public record AuthenticationRequest(
        String credential,
        String scheme,
        Map<String, Object> attributes
) {

    public AuthenticationRequest {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    public static AuthenticationRequest bearer(String token) {
        return new AuthenticationRequest(token, "Bearer", Map.of());
    }
}
