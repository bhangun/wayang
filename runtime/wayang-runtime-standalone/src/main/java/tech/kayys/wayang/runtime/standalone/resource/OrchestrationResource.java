package tech.kayys.wayang.runtime.standalone.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.kayys.gamelan.engine.tenant.TenantId;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinitionService;

import java.util.List;

/**
 * REST API for Wayang Orchestration
 */
@Path("/api/orchestration")
@Produces(MediaType.APPLICATION_JSON)
public class OrchestrationResource {

    private static final TenantId DEFAULT_TENANT = TenantId.of("default-tenant");

    @Inject
    WorkflowDefinitionService workflowDefinitionService;

    @GET
    @Path("/workflows")
    public Uni<List<WorkflowDefinition>> listWorkflows() {
        return workflowDefinitionService.list(DEFAULT_TENANT, true);
    }
}
