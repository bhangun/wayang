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

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import tech.kayys.wayang.agent.AgentTask;
import tech.kayys.wayang.control.service.AgentManager;
import tech.kayys.wayang.control.dto.CreateAgentRequest;

import java.util.UUID;

/**
 * REST API for AI Agent management.
 */
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
        return agentManager.createAgent(projectId, request)
                .map(agent -> Response.status(Response.Status.CREATED).entity(agent).build());
    }

    @POST
    @Path("/{agentId}/execute")
    public Uni<Response> executeTask(@PathParam("agentId") UUID agentId,
            @Valid tech.kayys.wayang.control.dto.AgentTask taskRequest,
            @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {
        AgentTask agentTask = new AgentTask(
                taskRequest.taskId(),
                taskRequest.instruction(),
                taskRequest.context(),
                java.util.Collections.emptyList());

        return agentManager.executeTask(agentId, agentTask)
                .map(result -> Response.ok(result).build());
    }
}
