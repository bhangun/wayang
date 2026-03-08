package tech.kayys.wayang.debugger.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.runtime.standalone.resource.ProjectsService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Optional debug endpoints that aggregate execution events and telemetry
 * for UI troubleshooting.
 */
@Path("/api/v1/debug/projects/{projectId}/executions/{executionId}")
@Produces(MediaType.APPLICATION_JSON)
public class ExecutionDebuggerResource {

    @Inject
    ProjectsService projectsService;

    @GET
    @Path("/events")
    public Response events(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId) {
        return projectsService.listExecutionEvents(projectId, executionId);
    }

    @GET
    @Path("/telemetry")
    public Response telemetry(
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
    @Path("/lineage")
    public Response lineage(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @QueryParam("view") String view,
            @QueryParam("nodeId") String nodeId,
            @QueryParam("sort") String sort,
            @QueryParam("limit") Integer limit,
            @QueryParam("offset") Integer offset,
            @QueryParam("fields") String fields,
            @QueryParam("include") String include) {
        return projectsService.getExecutionLineage(
                projectId, executionId, view, nodeId, sort, limit, offset, fields, include);
    }

    @GET
    @Path("/snapshot")
    public Response snapshot(
            @PathParam("projectId") String projectId,
            @PathParam("executionId") String executionId,
            @QueryParam("includeRaw") @DefaultValue("false") boolean includeRaw,
            @QueryParam("eventsLimit") @DefaultValue("200") int eventsLimit) {
        final Response statusResponse = projectsService.getExecutionStatus(projectId, executionId, null);
        final Response eventsResponse = projectsService.listExecutionEvents(projectId, executionId);
        final Response telemetryResponse = projectsService.getExecutionTelemetry(
                projectId, executionId, null, null, null, null, null, null, eventsLimit, includeRaw);
        final Response lineageResponse = projectsService.getExecutionLineage(
                projectId, executionId, "compact", null, null, 25, 0, null, "executionContext,status,updatedAt");

        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("projectId", projectId);
        payload.put("executionId", executionId);
        payload.put("status", readEntity(statusResponse));
        payload.put("events", readEntity(eventsResponse));
        payload.put("telemetry", readEntity(telemetryResponse));
        payload.put("lineage", readEntity(lineageResponse));
        return Response.ok(payload).build();
    }

    private Object readEntity(Response response) {
        if (response == null) {
            return Map.of("error", "no-response");
        }
        final int status = response.getStatus();
        final Object entity = response.getEntity();
        if (status >= 200 && status < 300) {
            return entity != null ? entity : Map.of();
        }
        final Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status);
        if (entity instanceof Map<?, ?> map) {
            error.putAll((Map<String, Object>) map);
        } else if (entity instanceof List<?> list) {
            error.put("items", list);
        } else {
            error.put("message", entity != null ? String.valueOf(entity) : "unknown");
        }
        return error;
    }
}
