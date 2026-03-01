package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.dto.designer.*;
import tech.kayys.wayang.control.service.DesignerService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.UUID;
import java.util.List;

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

    @POST
    @Path("/routes")
    @Operation(summary = "Create a new integration route")
    public Uni<Response> createRoute(CreateRouteRequest request) {
        return designerService.createRoute(request)
                .map(route -> Response.status(Response.Status.CREATED).entity(route).build());
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
    public Uni<Response> addNode(@PathParam("routeId") String routeId, AddNodeRequest request) {
        return Uni.createFrom().item(Response.ok().build());
    }

    @POST
    @Path("/routes/{routeId}/connections")
    @Operation(summary = "Add connection to route")
    public Uni<Response> addConnection(@PathParam("routeId") String routeId, AddConnectionRequest request) {
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
    public Uni<DeploymentResult> deployRoute(@PathParam("routeId") String routeId) {
        return designerService.deployRoute(routeId);
    }
}
