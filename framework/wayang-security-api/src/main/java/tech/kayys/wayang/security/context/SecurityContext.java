package tech.kayys.wayang.security.context;

import tech.kayys.wayang.security.identity.IdentityType;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.util.Map;
import java.util.Objects;

/**
 * The security context carried through the Wayang execution boundary.
 * Produced at authentication time and consumed by the authorization layer.
 */
public record SecurityContext(
        Principal principal,
        TenantContext tenant,
        Map<String, Object> attributes
) {

    public SecurityContext {
        Objects.requireNonNull(principal, "principal");
        Objects.requireNonNull(tenant,    "tenant");

        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    /** Creates an anonymous security context with no tenant. */
    public static SecurityContext anonymous() {
        return new SecurityContext(
                Principal.anonymous(),
                TenantContext.empty(),
                Map.of()
        );
    }

    /** Creates a system-level trusted context (for internal in-process calls). */
    public static SecurityContext system() {
        return new SecurityContext(
                Principal.system(),
                TenantContext.empty(),
                Map.of()
        );
    }

    public static SecurityContext of(Principal principal, TenantContext tenant) {
        return new SecurityContext(principal, tenant, Map.of());
    }

    public boolean isAnonymous() {
        return principal.type() == IdentityType.ANONYMOUS;
    }
}
