package tech.kayys.wayang.control.api;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.WorkflowManager;
import tech.kayys.wayang.control.dto.CreateTemplateRequest;

import java.util.UUID;

/**
 * REST API for Workflow Template management.
 */
@Path("/api/v1/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TemplateResource {

    @Inject
    WorkflowManager workflowManager;

    @POST
    public Uni<Response> createTemplate(@QueryParam("projectId") UUID projectId,
            @Valid CreateTemplateRequest request) {
        return workflowManager.createWorkflowTemplate(projectId, request)
                .map(template -> Response.status(Response.Status.CREATED).entity(template).build());
    }

    @GET
    @Path("/{templateId}")
    public Uni<Response> getTemplate(@PathParam("templateId") UUID templateId) {
        return workflowManager.getWorkflowTemplate(templateId)
                .map(template -> template != null ? Response.ok(template).build()
                        : Response.status(Response.Status.NOT_FOUND).build());
    }

    @POST
    @Path("/{templateId}/publish")
    public Uni<Response> publishTemplate(@PathParam("templateId") UUID templateId) {
        return workflowManager.publishWorkflowTemplate(templateId)
                .map(definitionId -> Response.ok(java.util.Map.of("workflowDefinitionId", definitionId)).build());
    }
}
