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

import io.quarkus.arc.properties.IfBuildProperty;
import io.smallrye.mutiny.Uni;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.control.dto.CreateProjectRequest;
import tech.kayys.wayang.control.dto.ProjectType;
import tech.kayys.wayang.control.dto.realtime.ControlPlaneRealtimeEvent;
import tech.kayys.wayang.schema.DefinitionType;
import tech.kayys.wayang.schema.WayangSpec;
import tech.kayys.wayang.schema.catalog.BuiltinSchemaCatalog;
import tech.kayys.wayang.schema.validator.SchemaValidationService;
import tech.kayys.wayang.schema.validator.ValidationResult;
import tech.kayys.wayang.runtime.standalone.resource.ProjectsService;

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
@Tag(name = "Projects", description = "Project/workspace management and project-scoped executions")
@IfBuildProperty(name = "wayang.runtime.standalone.projects-resource.enabled", stringValue = "false", enableIfMissing = true)
public class ProjectResource {

    private static final Logger LOG = Logger.getLogger(ProjectResource.class);
    private static final String DEFAULT_TENANT_ID = "community";

    @Inject
    ProjectManager projectManager;

    @Inject
    WayangDefinitionService definitionService;

    @Inject
    SchemaValidationService schemaValidationService;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Event<ControlPlaneRealtimeEvent> realtimeEvents;
    @Inject
    ProjectsService projectsService;

    @GET
    public List<Map<String, Object>> listProjects() {
        return projectsService.listProjects();
    }

    @GET
    @Path("/shareable")
    public Response listShareableProjects(
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId,
            @HeaderParam("X-User-Id") String userId,
            @QueryParam("mode") @DefaultValue("callable") String mode,
            @QueryParam("excludeProjectId") String excludeProjectId) {
        return projectsService.listShareableProjects(tenantId, userId, mode, excludeProjectId);
    }

    @POST
    public Response createProject(Map<String, Object> request) {
        return projectsService.createProject(request);
    }

    @GET
    @Path("/{projectId}")
    public Response getProject(@PathParam("projectId") String projectId) {
        return projectsService.getProject(projectId);
    }

    @GET
    @Path("/{projectId}/callable-contract")
    public Response getCallableContract(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId,
            @HeaderParam("X-User-Id") String userId) {
        return projectsService.getCallableContract(projectId, tenantId, userId);
    }

    @POST
    @Path("/{projectId}/validate-callable")
    public Response validateCallableContract(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId,
            @HeaderParam("X-User-Id") String userId,
            Map<String, Object> request) {
        return projectsService.validateCallableContract(projectId, tenantId, userId, request);
    }

    @POST
    @Path("/{projectId}/preview-output-bindings")
    public Response previewOutputBindings(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId,
            @HeaderParam("X-User-Id") String userId,
            Map<String, Object> request) {
        return projectsService.previewOutputBindings(projectId, tenantId, userId, request);
    }

    @PUT
    @Path("/{projectId}")
    public Response updateProject(
            @PathParam("projectId") String projectId,
            Map<String, Object> request) {
        return projectsService.updateProject(projectId, request);
    }

    @DELETE
    @Path("/{projectId}")
    public Response deleteProject(@PathParam("projectId") String projectId) {
        return projectsService.deleteProject(projectId);
    }

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

    /**
     * Execute a project-scoped WayangSpec payload directly.
     * Flow: validate -> create definition -> publish -> run.
     */
    @POST
    @Path("/{projectId}/execute-spec")
    @Operation(summary = "Execute WayangSpec payload in a project",
            description = "Validates a WayangSpec payload, creates a project definition, publishes it, and starts execution")
    public Uni<Response> executeProjectSpec(
            @PathParam("projectId") UUID projectId,
            @HeaderParam("X-Tenant-Id") String tenantId,
            @Valid ProjectExecutionRequest request) {
        return executeProjectSpecInternal(projectId, tenantId, request);
    }

    /**
     * REST-friendly alias for execute-spec.
     */
    @POST
    @Path("/{projectId}/executions")
    @Operation(summary = "Create project execution",
            description = "Alias for /execute-spec. Accepts the same request payload and starts execution")
    public Uni<Response> createExecution(
            @PathParam("projectId") UUID projectId,
            @HeaderParam("X-Tenant-Id") String tenantId,
            @Valid ProjectExecutionRequest request) {
        return executeProjectSpecInternal(projectId, tenantId, request);
    }

    private Uni<Response> executeProjectSpecInternal(
            UUID projectId,
            String tenantId,
            ProjectExecutionRequest request) {
        String resolvedTenant = resolveTenantId(tenantId);
        if (request == null || request.spec() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Request body with 'spec' is required"))
                    .build());
        }

        String schema = BuiltinSchemaCatalog.get(BuiltinSchemaCatalog.WAYANG_SPEC);
        Map<String, Object> specPayload = objectMapper.convertValue(
                request.spec(), new TypeReference<Map<String, Object>>() {});
        ValidationResult validation = schemaValidationService.validateSchema(schema, specPayload);
        if (!validation.isValid()) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "message", "WayangSpec validation failed",
                            "detail", validation.getMessage()))
                    .build());
        }

        String definitionName = (request.name() == null || request.name().isBlank())
                ? "project-" + projectId + "-spec"
                : request.name();
        String createdBy = (request.createdBy() == null || request.createdBy().isBlank())
                ? "api"
                : request.createdBy();
        boolean dryRun = Boolean.TRUE.equals(request.dryRun()) || Boolean.TRUE.equals(request.validateOnly());
        Map<String, Object> runtimeInputs = request.inputs() == null ? Map.of() : request.inputs();

        return projectManager.getProject(projectId, resolvedTenant)
                .flatMap(project -> {
                    if (project == null) {
                        return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND)
                                .entity(new ProjectExecutionErrorResponse("Project not found: " + projectId))
                                .build());
                    }

                    if (dryRun) {
                        return Uni.createFrom().item(Response.ok(new ProjectExecutionDryRunResponse(
                                projectId.toString(),
                                true,
                                true,
                                true,
                                BuiltinSchemaCatalog.WAYANG_SPEC,
                                definitionName,
                                runtimeInputs.size(),
                                "DRY_RUN_VALID"))
                                .build());
                    }

                    return definitionService
                            .create(
                                    resolvedTenant,
                                    projectId,
                                    definitionName,
                                    request.description(),
                                    DefinitionType.WORKFLOW_TEMPLATE,
                                    request.spec(),
                                    createdBy)
                            .flatMap(def -> definitionService.publish(def.definitionId, createdBy)
                                    .flatMap(published -> definitionService.run(published.definitionId, runtimeInputs)
                                            .map(executionId -> {
                                                realtimeEvents.fire(new ControlPlaneRealtimeEvent(
                                                        "project.execution.started",
                                                        "wayang",
                                                        BuiltinSchemaCatalog.WAYANG_SPEC,
                                                        Map.of(
                                                                "projectId", projectId.toString(),
                                                                "definitionId", published.definitionId.toString(),
                                                                "workflowDefinitionId",
                                                                published.workflowDefinitionId,
                                                                "executionId", executionId),
                                                        Map.of("source", "project-resource"),
                                                        Set.of(
                                                                "tenant:" + resolvedTenant,
                                                                "project:" + projectId)));

                                                return Response.accepted(new ProjectExecutionAcceptedResponse(
                                                        projectId.toString(),
                                                        published.definitionId.toString(),
                                                        published.workflowDefinitionId,
                                                        executionId,
                                                        "STARTED"))
                                                        .build();
                                            })));
                })
                .onFailure(IllegalArgumentException.class)
                .recoverWithItem(error -> Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ProjectExecutionErrorResponse(error.getMessage()))
                        .build())
                .onFailure(IllegalStateException.class)
                .recoverWithItem(error -> Response.status(Response.Status.CONFLICT)
                        .entity(new ProjectExecutionErrorResponse(error.getMessage()))
                        .build())
                .onFailure().recoverWithItem(error -> {
                    LOG.errorf(error, "Failed to execute WayangSpec for project %s tenant %s", projectId,
                            resolvedTenant);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(new ProjectExecutionErrorResponse("Failed to execute WayangSpec"))
                            .build();
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

record ProjectExecutionRequest(
        String name,
        String description,
        @NotNull @JsonAlias({ "wayangSpec", "workflowSpec" }) WayangSpec spec,
        Map<String, Object> inputs,
        String createdBy,
        Boolean dryRun,
        Boolean validateOnly) {
}

record ProjectExecutionErrorResponse(String message) {
}

record ProjectExecutionAcceptedResponse(
        String projectId,
        String definitionId,
        String workflowDefinitionId,
        String executionId,
        String status) {
}

record ProjectExecutionDryRunResponse(
        String projectId,
        boolean dryRun,
        boolean validated,
        boolean canExecute,
        String schemaId,
        String name,
        int inputCount,
        String status) {
}
