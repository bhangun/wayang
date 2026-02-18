package tech.kayys.wayang.control.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.agent.AgentExecutionResult;
import tech.kayys.wayang.control.service.AgentManager;
import tech.kayys.wayang.control.dto.CreateAgentRequest;
import tech.kayys.wayang.control.dto.AgentTask;
import tech.kayys.wayang.control.domain.AIAgent;

import java.util.UUID;

@Path("/v1/agents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentApi {

    private static final Logger LOG = LoggerFactory.getLogger(AgentApi.class);

    @Inject
    AgentManager agentManager;

    @POST
    public Response createAgent(@QueryParam("projectId") UUID projectId, CreateAgentRequest request) {
        LOG.info("Creating agent in project: {}", projectId);
        
        return agentManager.createAgent(projectId, request)
                .onItem().transform(agent -> Response.status(Response.Status.CREATED).entity(agent).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error creating agent", throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity(Response.Status.INTERNAL_SERVER_ERROR.getReasonPhrase())
                            .build();
                })
                .await().indefinitely();
    }

    @GET
    @Path("/{agentId}")
    public Response getAgent(@PathParam("agentId") UUID agentId) {
        LOG.debug("Getting agent: {}", agentId);
        
        return agentManager.getAgent(agentId)
                .onItem().transform(agent -> {
                    if (agent != null) {
                        return Response.ok(agent).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                })
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error getting agent: " + agentId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @POST
    @Path("/{agentId}/activate")
    public Response activateAgent(@PathParam("agentId") UUID agentId) {
        LOG.info("Activating agent: {}", agentId);
        
        return agentManager.activateAgent(agentId)
                .onItem().transform(agent -> Response.ok(agent).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error activating agent: " + agentId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @POST
    @Path("/{agentId}/deactivate")
    public Response deactivateAgent(@PathParam("agentId") UUID agentId) {
        LOG.info("Deactivating agent: {}", agentId);
        
        return agentManager.deactivateAgent(agentId)
                .onItem().transform(agent -> Response.ok(agent).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error deactivating agent: " + agentId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }

    @POST
    @Path("/{agentId}/execute")
    public Response executeTask(@PathParam("agentId") UUID agentId, AgentTask task) {
        LOG.info("Executing task with agent: {}", agentId);
        
        return agentManager.executeTask(agentId, task)
                .onItem().transform(result -> Response.ok(result).build())
                .onFailure().recoverWithItem(throwable -> {
                    LOG.error("Error executing task with agent: " + agentId, throwable);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
                })
                .await().indefinitely();
    }
}
