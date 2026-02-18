package tech.kayys.wayang.eip.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.eip.plugin.IntegrationDeployment;
import tech.kayys.wayang.eip.plugin.IntegrationPlugin;
import tech.kayys.wayang.eip.service.IntegrationService;

import java.util.List;
import java.util.Map;

@Path("/integration")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class IntegrationResource {

    @Inject
    IntegrationService integrationService;

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of("status", "UP")).build();
    }

    @GET
    @Path("/plugins")
    public Response plugins() {
        List<IntegrationPlugin> plugins = integrationService.listPlugins();
        return Response.ok(plugins.stream().map(p -> Map.of(
                "id", p.id(),
                "description", p.description())).toList()).build();
    }

    @GET
    @Path("/deployments")
    public Response deployments() {
        return Response.ok(integrationService.listDeployments()).build();
    }

    @POST
    @Path("/deploy/{pluginId}")
    public Response deploy(@PathParam("pluginId") String pluginId, Map<String, Object> options) {
        IntegrationDeployment deployment = integrationService.deploy(pluginId, options);
        return Response.status(Response.Status.CREATED).entity(deployment).build();
    }

    @DELETE
    @Path("/deployments/{deploymentId}")
    public Response undeploy(@PathParam("deploymentId") String deploymentId) {
        boolean removed = integrationService.undeploy(deploymentId);
        if (!removed) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Deployment not found", "deploymentId", deploymentId))
                    .build();
        }
        return Response.noContent().build();
    }
}
