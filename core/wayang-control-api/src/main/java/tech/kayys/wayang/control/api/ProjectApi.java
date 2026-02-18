package tech.kayys.wayang.control.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.dto.CreateProjectRequest;
import tech.kayys.wayang.control.domain.WayangProject;

import java.util.List;
import java.util.UUID;

@Path("/v1/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectApi {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectApi.class);

    @Inject
    ProjectManager projectManager;

    @POST
    public Response createProject(CreateProjectRequest request) {
        LOG.info("Creating project: {}", request.projectName());
        
        return projectManager.createProject(request)
                .onItem().transform(project -> Response.status(Response.Status.CREATED).entity(project).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error creating project", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                            .build();
                })
                .await().indefinitely();
    }

    @GET
    @Path("/{projectId}")
    public Response getProject(@PathParam("projectId") UUID projectId, @QueryParam("tenantId") String tenantId) {
        LOG.debug("Getting project: {}", projectId);
        
        return projectManager.getProject(projectId, tenantId)
                .onItem().transform(project -> {
                    if (project != null) {
                        return Response.ok(project).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error getting project: " + projectId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @GET
    public Response listProjects(@QueryParam("tenantId") String tenantId, @QueryParam("type") String type) {
        LOG.debug("Listing projects for tenant: {}", tenantId);
        
        var projectType = type != null ? tech.kayys.wayang.control.dto.ProjectType.valueOf(type) : null;
        
        return projectManager.listProjects(tenantId, projectType)
                .onItem().transform(projects -> Response.ok(projects).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error listing projects for tenant: " + tenantId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @DELETE
    @Path("/{projectId}")
    public Response deleteProject(@PathParam("projectId") UUID projectId, @QueryParam("tenantId") String tenantId) {
        LOG.info("Deleting project: {}", projectId);
        
        return projectManager.deleteProject(projectId, tenantId)
                .onItem().transform(success -> {
                    if (success) {
                        return Response.noContent().build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error deleting project: " + projectId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }
}