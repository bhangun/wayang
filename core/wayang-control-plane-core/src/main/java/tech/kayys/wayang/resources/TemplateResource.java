package tech.kayys.wayang.resources;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.control.service.WorkflowManager;
import tech.kayys.wayang.project.dto.CreateTemplateRequest;
import tech.kayys.wayang.project.dto.PublishResponse;

import java.util.UUID;

@Path("/api/v1/control-plane/templates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TemplateResource {

        @Inject
        WorkflowManager workflowManager;

        @POST
        public Uni<Response> createTemplate(
                        @QueryParam("projectId") UUID projectId,
                        @Valid CreateTemplateRequest request,
                        @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {

                return workflowManager.createWorkflowTemplate(projectId, request)
                                .map(template -> Response.status(Response.Status.CREATED).entity(template).build());
        }

        @POST
        @Path("/{templateId}/publish")
        public Uni<Response> publishTemplate(
                        @PathParam("templateId") UUID templateId,
                        @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {

                return workflowManager.publishWorkflowTemplate(templateId)
                                .map(workflowDefId -> Response.ok(
                                                new PublishResponse(
                                                                true,
                                                                workflowDefId,
                                                                "Template published successfully"))
                                                .build());
        }

        // Execute logic to be handled by OrchestrationResource or dedicated RunResource
}
