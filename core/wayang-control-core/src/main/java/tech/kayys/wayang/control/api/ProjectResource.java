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
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.dto.CreateProjectRequest;
import tech.kayys.wayang.control.dto.ProjectType;

import java.util.UUID;

/**
 * REST API for Project and Workspace management.
 */
@Path("/api/v1/projects")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProjectResource {

        @Inject
        ProjectManager projectManager;

        @POST
        public Uni<Response> createProject(@Valid CreateProjectRequest request) {
                return projectManager.createProject(request)
                                .map(project -> Response.status(Response.Status.CREATED).entity(project).build());
        }

        @GET
        @Path("/{projectId}")
        public Uni<Response> getProject(@PathParam("projectId") UUID projectId,
                        @HeaderParam("X-Tenant-Id") String tenantId) {
                return projectManager.getProject(projectId, tenantId)
                                .map(project -> project != null ? Response.ok(project).build()
                                                : Response.status(Response.Status.NOT_FOUND).build());
        }

        @GET
        public Uni<Response> listProjects(@HeaderParam("X-Tenant-Id") String tenantId,
                        @QueryParam("type") ProjectType type) {
                return projectManager.listProjects(tenantId, type)
                                .map(projects -> Response.ok(projects).build());
        }

        @DELETE
        @Path("/{projectId}")
        public Uni<Response> deleteProject(@PathParam("projectId") UUID projectId,
                        @HeaderParam("X-Tenant-Id") String tenantId) {
                return projectManager.deleteProject(projectId, tenantId)
                                .map(success -> success ? Response.noContent().build()
                                                : Response.status(Response.Status.NOT_FOUND).build());
        }
}
