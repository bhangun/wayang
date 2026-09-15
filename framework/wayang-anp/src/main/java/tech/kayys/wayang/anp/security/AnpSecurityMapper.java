package tech.kayys.wayang.anp.security;

import tech.kayys.wayang.anp.auth.AnpHttpSignature;
import tech.kayys.wayang.anp.identity.DidWbaIdentity;
import tech.kayys.wayang.security.delegation.DelegationContext;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.util.Map;
import java.util.Optional;

/**
 * Maps incoming ANP authentication data into an immutable {@link SecurityContextSnapshot}.
 */
public final class AnpSecurityMapper {

    private final AnpPrincipalResolver principalResolver;

    public AnpSecurityMapper(AnpPrincipalResolver principalResolver) {
        this.principalResolver = principalResolver;
    }

    public SecurityContextSnapshot map(
            DidWbaIdentity callerDid,
            AnpHttpSignature signature,
            String tenantId,
            Optional<DelegationContext> delegation) {

        Principal principal = principalResolver.resolve(callerDid);
        TenantContext tenant = (tenantId == null || tenantId.isBlank())
                ? TenantContext.empty()
                : TenantContext.of(tenantId);

        Map<String, Object> attributes = Map.of(
                "anp.keyId", signature.keyId(),
                "anp.algorithm", signature.algorithm(),
                "anp.verified", true
        );

        return new SecurityContextSnapshot(principal, tenant, delegation, attributes);
    }
}
