package tech.kayys.wayang.runtime.standalone.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import tech.kayys.wayang.engine.gamelan.GamelanWorkflowEngine;
import tech.kayys.gamelan.engine.workflow.WorkflowDefinition;

import java.util.List;

/**
 * REST API for Wayang Orchestration
 */
@Path("/api/orchestration")
@Produces(MediaType.APPLICATION_JSON)
public class OrchestrationResource {

    @Inject
    GamelanWorkflowEngine engine;

    @GET
    @Path("/workflows")
    public Uni<List<WorkflowDefinition>> listWorkflows() {
        return engine.listWorkflows();
    }
}
