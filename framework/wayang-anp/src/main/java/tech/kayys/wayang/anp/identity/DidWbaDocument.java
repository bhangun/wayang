package tech.kayys.wayang.anp.identity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * W3C-compliant DID Document representation for a DID:WBA identity.
 */
public record DidWbaDocument(
        String id,
        List<DidWbaVerificationMethod> verificationMethods,
        List<String> authentication,
        Map<String, String> services
) {
    public DidWbaDocument {
        Objects.requireNonNull(id, "id");
        verificationMethods = verificationMethods == null ? List.of() : List.copyOf(verificationMethods);
        authentication = authentication == null ? List.of() : List.copyOf(authentication);
        services = services == null ? Map.of() : Map.copyOf(services);
    }

    public Optional<DidWbaVerificationMethod> findVerificationMethod(String methodId) {
        return verificationMethods.stream()
                .filter(m -> m.id().equals(methodId) || m.id().endsWith("#" + methodId))
                .findFirst();
    }
}
