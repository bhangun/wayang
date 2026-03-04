package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Event;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.dto.AddConnectionRequest;
import tech.kayys.wayang.control.dto.AddNodeRequest;
import tech.kayys.wayang.control.dto.CreateRouteRequest;
import tech.kayys.wayang.control.dto.DeploymentResult;
import tech.kayys.wayang.control.dto.DesignerValidationResult;
import tech.kayys.wayang.control.dto.GeneratedRoute;
import tech.kayys.wayang.control.dto.realtime.ControlPlaneRealtimeEvent;
import tech.kayys.wayang.control.service.DesignerService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;
import java.util.Set;

/**
 * Designer Backend API - For visual route design and deployment.
 */
@Path("/api/v1/designer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Control Plane - Designer", description = "Visual designer backend APIs")
public class DesignerResource {

    @jakarta.inject.Inject
    DesignerService designerService;

    @jakarta.inject.Inject
    Event<ControlPlaneRealtimeEvent> realtimeEvents;

    @POST
    @Path("/routes")
    @Operation(summary = "Create a new integration route")
    public Uni<Response> createRoute(CreateRouteRequest request,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantIdHeader) {
        return designerService.createRoute(request)
                .map(route -> {
                    String tenantId = resolveTenant(request.tenantId(), tenantIdHeader);
                    String routeId = route != null && route.routeId() != null ? route.routeId() : "unknown";
                    emitDesignerEvent("designer.route.created", tenantId, routeId, Map.of("route", route));
                    return Response.status(Response.Status.CREATED).entity(route).build();
                });
    }

    @GET
    @Path("/routes/{routeId}")
    @Operation(summary = "Get route design")
    public Uni<Response> getRoute(@PathParam("routeId") String routeId) {
        return Uni.createFrom().item(Response.ok().build());
    }

    @POST
    @Path("/routes/{routeId}/nodes")
    @Operation(summary = "Add node to route")
    public Uni<Response> addNode(@PathParam("routeId") String routeId, AddNodeRequest request,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId) {
        emitDesignerEvent("designer.node.added", tenantId, routeId, Map.of("routeId", routeId, "node", request));
        return Uni.createFrom().item(Response.ok().build());
    }

    @POST
    @Path("/routes/{routeId}/connections")
    @Operation(summary = "Add connection to route")
    public Uni<Response> addConnection(@PathParam("routeId") String routeId, AddConnectionRequest request,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId) {
        emitDesignerEvent("designer.connection.added", tenantId, routeId,
                Map.of("routeId", routeId, "connection", request));
        return Uni.createFrom().item(Response.ok().build());
    }

    @GET
    @Path("/routes/{routeId}/validate")
    @Operation(summary = "Validate route design")
    public Uni<DesignerValidationResult> validateRoute(@PathParam("routeId") String routeId) {
        return designerService.validateRoute(routeId);
    }

    @POST
    @Path("/routes/{routeId}/generate")
    @Operation(summary = "Generate route execution logic")
    public Uni<GeneratedRoute> generateRoute(@PathParam("routeId") String routeId) {
        return designerService.generateRoute(routeId);
    }

    @POST
    @Path("/routes/{routeId}/deploy")
    @Operation(summary = "Deploy route to Gamelan runtime")
    public Uni<DeploymentResult> deployRoute(@PathParam("routeId") String routeId,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId) {
        return designerService.deployRoute(routeId)
                .invoke(result -> emitDesignerEvent(
                        "designer.route.deployed",
                        tenantId,
                        routeId,
                        Map.of("routeId", routeId, "deployment", result)));
    }

    private void emitDesignerEvent(String type, String tenantId, String routeId, Map<String, Object> payload) {
        realtimeEvents.fire(new ControlPlaneRealtimeEvent(
                type,
                "workflow",
                "workflow-spec",
                payload,
                Map.of("source", "designer-resource"),
                Set.of("tenant:" + tenantId, "route:" + routeId)));
    }

    private String resolveTenant(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback == null || fallback.isBlank() ? "community" : fallback;
    }
}
