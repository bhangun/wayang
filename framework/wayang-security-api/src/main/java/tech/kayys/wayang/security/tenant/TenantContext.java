package tech.kayys.wayang.security.tenant;

import java.util.Map;

/**
 * Multi-tenancy context carried through the Wayang security boundary.
 * Tenant isolation is enforced by the authorization layer using this context.
 */
public record TenantContext(
        String tenantId,
        Map<String, Object> attributes
) {

    public TenantContext {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    /** Returns a context without a tenant (suitable for system/anonymous calls). */
    public static TenantContext empty() {
        return new TenantContext(null, Map.of());
    }

    public static TenantContext of(String tenantId) {
        return new TenantContext(tenantId, Map.of());
    }

    public boolean hasTenant() {
        return tenantId != null && !tenantId.isBlank();
    }
}
