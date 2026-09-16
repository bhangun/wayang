package tech.kayys.wayang.api.rest.security;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.security.delegation.DelegationAudience;
import tech.kayys.wayang.security.delegation.DelegationConstraints;
import tech.kayys.wayang.security.delegation.DelegationContext;
import tech.kayys.wayang.security.delegation.DelegationRequest;
import tech.kayys.wayang.security.delegation.DelegationService;
import tech.kayys.wayang.security.identity.Principal;
import tech.kayys.wayang.security.propagation.SecurityContextSnapshot;
import tech.kayys.wayang.security.tenant.TenantContext;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Endpoint for managing Wayang Security Delegations.
 */
@Path("/api/v1/security")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SecurityDelegationResource {

    @Inject
    DelegationService delegationService;

    /**
     * Creates an attenuated child delegation.
     */
    @POST
    @Path("/delegations")
    public Response createDelegation(
            @HeaderParam("X-Tenant-Id") String tenantId,
            @HeaderParam("X-Caller-Id") String callerId,
            CreateDelegationRequest request) {

        if (request == null || request.audience() == null || request.audience().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "audience is required"))
                    .build();
        }

        try {
            // Build root caller snapshot
            String principalId = callerId != null ? callerId : "system";
            Principal principal = Principal.agent(principalId, principalId);
            TenantContext tenant = tenantId != null ? TenantContext.of(tenantId) : TenantContext.empty();
            SecurityContextSnapshot parent = new SecurityContextSnapshot(principal, tenant, Optional.empty(), Map.of());

            // Build delegation constraints
            List<String> caps = request.allowedCapabilities() != null ? request.allowedCapabilities() : List.of("*");
            List<String> acts = request.allowedActions() != null ? request.allowedActions() : List.of("*");
            int hops = request.maxHops() > 0 ? request.maxHops() : 3;
            DelegationConstraints constraints = new DelegationConstraints(caps, acts, hops);

            long ttl = request.lifetimeSeconds() > 0 ? request.lifetimeSeconds() : 3600;
            DelegationRequest delegationRequest = new DelegationRequest(
                    DelegationAudience.of(request.audience()),
                    constraints,
                    Duration.ofSeconds(ttl),
                    Map.of()
            );

            DelegationContext result = delegationService.delegate(parent, delegationRequest)
                    .toCompletableFuture()
                    .join();

            DelegationResponse response = new DelegationResponse(
                    result.id().value(),
                    result.audience().targetAgentIds(),
                    result.constraints().allowedCapabilities(),
                    result.constraints().allowedActions(),
                    result.constraints().maxHops(),
                    result.issuedAt().toString(),
                    result.expiresAt().toString(),
                    result.parent().map(p -> p.value()).orElse(null)
            );

            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Delegation failed: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/status")
    public Response status() {
        return Response.ok(Map.of(
                "security", "Wayang Multi-Tenant Attenuated Security",
                "version", "0.0.1",
                "delegationEnabled", true
        )).build();
    }

    public record CreateDelegationRequest(
            String audience,
            List<String> allowedCapabilities,
            List<String> allowedActions,
            int maxHops,
            long lifetimeSeconds
    ) {}

    public record DelegationResponse(
            String delegationId,
            List<String> targetAgentIds,
            List<String> allowedCapabilities,
            List<String> allowedActions,
            int maxHops,
            String issuedAt,
            String expiresAt,
            String parentDelegationId
    ) {}
}
