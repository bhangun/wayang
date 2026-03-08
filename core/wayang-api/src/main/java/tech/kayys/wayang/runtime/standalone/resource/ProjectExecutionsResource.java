package tech.kayys.wayang.runtime.standalone.resource;

import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/v1/projects")
@PermitAll
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@IfBuildProperty(name = "wayang.runtime.standalone.projects-resource.enabled", stringValue = "true")
public class ProjectExecutionsResource {

    @Inject
    ProjectsService projectsService;

    @GET
    @Path("/{projectId}/executions")
    public Response listExecutions(@PathParam("projectId") String projectId) {
        return projectsService.listExecutions(projectId);
    }

    @GET
    @Path("/{projectId}/executions/{executionId}")
    public Response getExecutionStatus(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @HeaderParam("If-None-Match") String ifNoneMatch) {
        return projectsService.getExecutionStatus(projectId, executionId, ifNoneMatch);
    }

    @GET
    @Path("/{projectId}/executions/{executionId}/events")
    public Response listExecutionEvents(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId) {
        return projectsService.listExecutionEvents(projectId, executionId);
    }

    @GET
    @Path("/{projectId}/executions/{executionId}/telemetry")
    public Response getExecutionTelemetry(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @QueryParam("nodeId") String nodeId,
            @QueryParam("type") String type,
            @QueryParam("groupBy") String groupBy,
            @QueryParam("sort") String sort,
            @QueryParam("limit") Integer limit,
            @QueryParam("includeRaw") @DefaultValue("false") boolean includeRaw) {
        return projectsService.getExecutionTelemetry(
                projectId, executionId, from, to, nodeId, type, groupBy, sort, limit, includeRaw);
    }

    @GET
    @Path("/{projectId}/executions/{executionId}/lineage")
    public Response getExecutionLineage(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @QueryParam("view") @DefaultValue("full") String view,
            @QueryParam("nodeId") String nodeId,
            @QueryParam("sort") String sort,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("fields") String fields,
            @QueryParam("include") String include) {
        return projectsService.getExecutionLineage(
                projectId, executionId, view, nodeId, sort, limit, offset, fields, include);
    }

    @POST
    @Path("/{projectId}/execute-spec")
    public Response executeProjectSpec(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId,
            @HeaderParam("X-User-Id") String userId,
            @HeaderParam("X-Request-Id") String requestId,
            Map<String, Object> request) {
        return projectsService.executeProjectSpec(projectId, tenantId, userId, requestId, request);
    }

    @POST
    @Path("/{projectId}/executions")
    public Response createExecution(
            @PathParam("projectId") String projectId,
            @HeaderParam("X-Tenant-Id") @DefaultValue("community") String tenantId,
            @HeaderParam("X-User-Id") String userId,
            @HeaderParam("Idempotency-Key") String idempotencyKey,
            @HeaderParam("X-Idempotency-Key") String xIdempotencyKey,
            @HeaderParam("X-Request-Id") String requestId,
            Map<String, Object> request) {
        return projectsService.createExecution(
                projectId, tenantId, userId, idempotencyKey, xIdempotencyKey, requestId, request);
    }

    @POST
    @Path("/{projectId}/executions/{executionId}/stop")
    public Response stopExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @HeaderParam("If-Match") String ifMatch,
            Map<String, Object> request) {
        return projectsService.stopExecution(projectId, executionId, ifMatch, request);
    }

    @POST
    @Path("/{projectId}/executions/{executionId}/resume")
    public Response resumeExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @HeaderParam("If-Match") String ifMatch,
            Map<String, Object> request) {
        return projectsService.resumeExecution(projectId, executionId, ifMatch, request);
    }

    @DELETE
    @Path("/{projectId}/executions/{executionId}")
    public Response deleteExecution(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @HeaderParam("If-Match") String ifMatch,
            @QueryParam("expectedVersion") Long expectedVersionQuery) {
        return projectsService.deleteExecution(projectId, executionId, ifMatch, expectedVersionQuery);
    }
}
