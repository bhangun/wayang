/*
 * PolyForm Noncommercial License 1.0.0
 *
 * Copyright (c) 2026 Kayys.tech
 *
 * This software is licensed for non-commercial use only.
 * You may use, modify, and distribute this software for personal,
 * educational, or research purposes.
 *
 * Commercial use, including SaaS or revenue-generating services,
 * requires a separate commercial license from Kayys.tech.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND.
 *
 * @author Bhangun
 */

package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.dto.CreateProjectRequest;
import tech.kayys.wayang.control.dto.ProjectType;
import tech.kayys.wayang.control.dto.realtime.ControlPlaneRealtimeEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * REST API for Project and Workspace management.
 */
@Path("/api/v1/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

    private static final Logger LOG = Logger.getLogger(ProjectResource.class);
    private static final String DEFAULT_TENANT_ID = "community";

    @Inject
    ProjectManager projectManager;

    @Inject
    Event<ControlPlaneRealtimeEvent> realtimeEvents;

    @POST
    public Uni<Response> createProject(@Valid CreateProjectRequest request) {
        return projectManager.createProject(request)
                .map(project -> {
                    emitProjectEvent(
                            "project.created",
                            project.tenantId(),
                            project.projectId().toString(),
                            Map.of("project", project));
                    return Response.status(Response.Status.CREATED).entity(project).build();
                });
    }

    @GET
    @Path("/{projectId}")
    public Uni<Response> getProject(@PathParam("projectId") UUID projectId,
            @HeaderParam("X-Tenant-Id") String tenantId) {
        return projectManager.getProject(projectId, resolveTenantId(tenantId))
                .map(project -> project != null ? Response.ok(project).build()
                        : Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    public Uni<Response> listProjects(@HeaderParam("X-Tenant-Id") String tenantId,
            @QueryParam("type") ProjectType type) {
        String resolvedTenantId = resolveTenantId(tenantId);
        return projectManager.listProjects(resolvedTenantId, type)
                .map(projects -> Response.ok(projects).build())
                .onFailure().recoverWithItem(error -> {
                    LOG.warnf(error, "Failed to list projects for tenant %s. Returning empty list in standalone mode.",
                            resolvedTenantId);
                    return Response.ok(List.of()).build();
                });
    }

    @DELETE
    @Path("/{projectId}")
    public Uni<Response> deleteProject(@PathParam("projectId") UUID projectId,
            @HeaderParam("X-Tenant-Id") String tenantId) {
        String resolvedTenant = resolveTenantId(tenantId);
        return projectManager.deleteProject(projectId, resolvedTenant)
                .map(success -> {
                    if (success) {
                        emitProjectEvent(
                                "project.deleted",
                                resolvedTenant,
                                projectId.toString(),
                                Map.of("projectId", projectId.toString(), "tenantId", resolvedTenant));
                        return Response.noContent().build();
                    }
                    return Response.status(Response.Status.NOT_FOUND).build();
                });
    }

    private static String resolveTenantId(String tenantId) {
        return (tenantId == null || tenantId.isBlank()) ? DEFAULT_TENANT_ID : tenantId;
    }

    private void emitProjectEvent(String type, String tenantId, String projectId, Map<String, Object> payload) {
        realtimeEvents.fire(new ControlPlaneRealtimeEvent(
                type,
                "workflow",
                "workflow-spec",
                payload,
                Map.of("source", "project-resource"),
                Set.of("tenant:" + tenantId, "project:" + projectId)));
    }
}
