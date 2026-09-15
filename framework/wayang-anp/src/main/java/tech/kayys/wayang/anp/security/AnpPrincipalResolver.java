package tech.kayys.wayang.anp.security;

import tech.kayys.wayang.anp.identity.DidWbaIdentity;
import tech.kayys.wayang.security.identity.IdentityType;
import tech.kayys.wayang.security.identity.Principal;

import java.util.Map;

/**
 * Resolves a Wayang {@link Principal} from a verified {@link DidWbaIdentity}.
 */
public final class AnpPrincipalResolver {

    public Principal resolve(DidWbaIdentity did) {
        String name = did.pathSegments().isEmpty()
                ? did.host()
                : did.pathSegments().get(did.pathSegments().size() - 1);

        return new Principal(
                did.raw(),
                name,
                IdentityType.AGENT,
                Map.of("did", did.raw(), "protocol", "anp/1.1", "host", did.host())
        );
    }
}
