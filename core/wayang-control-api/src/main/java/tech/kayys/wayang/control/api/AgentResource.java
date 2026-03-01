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
import tech.kayys.wayang.control.dto.AgentTask;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.control.domain.WayangDefinition;
import tech.kayys.wayang.control.dto.CreateAgentRequest;
import tech.kayys.wayang.schema.DefinitionType;
import tech.kayys.wayang.schema.WayangSpec;

import java.util.Map;
import java.util.UUID;

/**
 * REST API for AI Agent management.
 */
@Path("/api/v1/agents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgentResource {

        @Inject
        WayangDefinitionService definitionService;

        @POST
        public Uni<Response> createAgent(@QueryParam("projectId") UUID projectId,
                        @Valid CreateAgentRequest request,
                        @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {

                WayangSpec spec = new WayangSpec();
                spec.getAgents().add(Map.of(
                                "name", request.agentName(),
                                "type", request.agentType(),
                                "tools", request.tools(),
                                "capabilities", request.capabilities(),
                                "llm", request.llmConfig(),
                                "memory", request.memoryConfig(),
                                "guardrails", request.guardrails()));

                return definitionService.create(tenantId, projectId, request.agentName(),
                                request.description(), DefinitionType.AI_AGENT, spec, "system")
                                .map(def -> Response.status(Response.Status.CREATED).entity(def).build());
        }

        @POST
        @Path("/{agentId}/execute")
        public Uni<Response> executeTask(@PathParam("agentId") UUID agentId,
                        @Valid tech.kayys.wayang.control.dto.AgentTask taskRequest,
                        @HeaderParam("X-Tenant-Id") @DefaultValue("default") String tenantId) {

                Map<String, Object> inputs = Map.of(
                                "taskId", taskRequest.taskId(),
                                "instruction", taskRequest.instruction(),
                                "context", taskRequest.context());

                return definitionService.run(agentId, inputs)
                                .map(executionId -> Response.ok(Map.of("executionId", executionId)).build());
        }
}
