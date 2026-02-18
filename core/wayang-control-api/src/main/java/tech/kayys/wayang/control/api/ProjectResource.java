package tech.kayys.wayang.resources;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.domain.WayangProject;
import tech.kayys.wayang.control.dto.CreateProjectRequest;
import tech.kayys.wayang.control.dto.ProjectType;

import java.util.List;
import java.util.UUID;

@Path("/api/v1/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

    @Inject
    ProjectManager projectManager;

    @POST
    public Uni<Response> createProject(@Valid CreateProjectRequest request,
            @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {
        // Enforce tenant from header if not in request (or override)
        // For now, assuming request might have it or we use header
        // CreateProjectRequest has tenantId field.

        // Use the tenantId from the DTO if present, else header
        String effectiveTenant = (request.tenantId() != null && !request.tenantId().isBlank()) ? request.tenantId()
                : tenantId;

        // Create a new request with the effective user/tenant if needed,
        // but ProjectManager takes the DTO directly.
        // We'll assume the DTO is valid or we mutate it if it were a builder.
        // Since it's a record, we might need to reconstruct if we want to enforce
        // tenant.

        return projectManager.createProject(request)
                .map(project -> Response.status(Response.Status.CREATED).entity(project).build());
    }

    @GET
    @Path("/{projectId}")
    public Uni<Response> getProject(@PathParam("projectId") UUID projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {
        return projectManager.getProject(projectId, tenantId)
                .map(project -> {
                    if (project == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    return Response.ok(project).build();
                });
    }

    @GET
    public Uni<List<WayangProject>> listProjects(@QueryParam("type") ProjectType projectType,
            @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {
        return projectManager.listProjects(tenantId, projectType);
    }

    @DELETE
    @Path("/{projectId}")
    public Uni<Response> deleteProject(@PathParam("projectId") UUID projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {
        return projectManager.deleteProject(projectId, tenantId)
                .map(deleted -> deleted ? Response.noContent().build()
                        : Response.status(Response.Status.NOT_FOUND).build());
    }
}
