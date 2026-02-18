package tech.kayys.wayang.control.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.service.WorkflowManager;
import tech.kayys.wayang.control.dto.CreateTemplateRequest;
import tech.kayys.wayang.control.domain.WorkflowTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/v1/workflows")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WorkflowApi {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowApi.class);

    @Inject
    WorkflowManager workflowManager;

    @POST
    public Response createWorkflowTemplate(@QueryParam("projectId") UUID projectId, CreateTemplateRequest request) {
        LOG.info("Creating workflow template in project: {}", projectId);
        
        return workflowManager.createWorkflowTemplate(projectId, request)
                .onItem().transform(template -> Response.status(Response.Status.CREATED).entity(template).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error creating workflow template", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                            .build();
                })
                .await().indefinitely();
    }

    @GET
    @Path("/{templateId}")
    public Response getWorkflowTemplate(@PathParam("templateId") UUID templateId) {
        LOG.debug("Getting workflow template: {}", templateId);
        
        return workflowManager.getWorkflowTemplate(templateId)
                .onItem().transform(template -> {
                    if (template != null) {
                        return Response.ok(template).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error getting workflow template: " + templateId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @POST
    @Path("/{templateId}/publish")
    public Response publishWorkflowTemplate(@PathParam("templateId") UUID templateId) {
        LOG.info("Publishing workflow template: {}", templateId);
        
        return workflowManager.publishWorkflowTemplate(templateId)
                .onItem().transform(workflowDefId -> Response.ok().entity(Map.of("workflowDefinitionId", workflowDefId)).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error publishing workflow template: " + templateId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Map.of("error", "Failed to publish workflow template"))
                            .build();
                })
                .await().indefinitely();
    }

    @GET
    public Response listAllTemplates(@QueryParam("tenantId") String tenantId) {
        LOG.debug("Listing all workflow templates for tenant: {}", tenantId);
        
        return workflowManager.listAllTemplates(tenantId)
                .onItem().transform(templates -> Response.ok(templates).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error listing workflow templates for tenant: " + tenantId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }
}