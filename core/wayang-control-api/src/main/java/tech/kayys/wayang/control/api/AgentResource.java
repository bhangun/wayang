package tech.kayys.wayang.resources;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.agent.AgentTask;
import tech.kayys.wayang.control.service.AgentManager;
import tech.kayys.wayang.control.dto.CreateAgentRequest;

import java.util.Collections;
import java.util.UUID;
import java.util.List;

@Path("/api/v1/agents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentResource {

        @Inject
        AgentManager agentManager;

        @POST
        public Uni<Response> createAgent(@QueryParam("projectId") UUID projectId,
                        @Valid CreateAgentRequest request,
                        @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {
                // Enforce tenant? AgentManager.createAgent takes (projectId, request).
                // We assume projectId ownership check handles tenant or we add it to
                // AgentManager later.

                return agentManager.createAgent(projectId, request)
                                .map(agent -> Response.status(Response.Status.CREATED).entity(agent).build());
        }

        // Additional get methods

        @POST
        @Path("/{agentId}/execute")
        public Uni<Response> executeTask(@PathParam("agentId") UUID agentId,
                        @Valid tech.kayys.wayang.control.dto.AgentTask taskRequest,
                        @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {
                AgentTask agentTask = new AgentTask(
                                taskRequest.taskId(),
                                taskRequest.instruction(),
                                taskRequest.context(),
                                Collections.emptyList());

                return agentManager.executeTask(agentId, agentTask)
                                .map(result -> Response.ok(result).build());
        }
}
