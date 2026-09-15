package tech.kayys.wayang.a2a.execution;

import tech.kayys.wayang.security.context.SecurityContext;
import tech.kayys.wayang.security.identity.IdentityType;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.util.Map;
import java.util.Optional;

/**
 * Maps A2A transport credentials and headers into a protocol-neutral {@link SecurityContextSnapshot}.
 */
public final class A2ASecurityMapper {

    private A2ASecurityMapper() {}

    public static SecurityContextSnapshot fromA2A(
            String callerId,
            String callerName,
            String tenantId,
            Map<String, Object> claims
    ) {
        IdentityType type = callerId != null && callerId.startsWith("agent-")
                ? IdentityType.AGENT
                : IdentityType.USER;

        Principal principal = new Principal(
                callerId != null ? callerId : "anonymous",
                callerName != null ? callerName : "anonymous",
                callerId != null ? type : IdentityType.ANONYMOUS,
                claims != null ? claims : Map.of()
        );

        TenantContext tenant = tenantId != null
                ? TenantContext.of(tenantId)
                : TenantContext.empty();

        SecurityContext context = SecurityContext.of(principal, tenant);
        return SecurityContextSnapshot.from(context, Optional.empty());
    }
}
