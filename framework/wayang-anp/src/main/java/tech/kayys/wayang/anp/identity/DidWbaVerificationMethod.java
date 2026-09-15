package tech.kayys.wayang.anp.identity;

import java.util.Map;
import java.util.Objects;

/**
 * A cryptographic verification method declared in a DID:WBA document.
 */
public record DidWbaVerificationMethod(
        String id,
        String type,
        String controller,
        String publicKeyMultibase,
        Map<String, Object> publicKeyJwk
) {
    public DidWbaVerificationMethod {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(controller, "controller");
        publicKeyJwk = publicKeyJwk == null ? Map.of() : Map.copyOf(publicKeyJwk);
    }

    public static DidWbaVerificationMethod ed25519(String id, String controller, String publicKeyMultibase) {
        return new DidWbaVerificationMethod(id, "Ed25519VerificationKey2020", controller, publicKeyMultibase, Map.of());
    }

    public static DidWbaVerificationMethod jwk(String id, String controller, Map<String, Object> jwk) {
        return new DidWbaVerificationMethod(id, "JsonWebKey2020", controller, null, jwk);
    }
}
