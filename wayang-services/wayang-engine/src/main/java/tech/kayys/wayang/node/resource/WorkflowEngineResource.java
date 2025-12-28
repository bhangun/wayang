package tech.kayys.wayang.node.resource;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.TimeoutException;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import tech.kayys.wayang.engine.AgentExecutionEvent;
import tech.kayys.wayang.engine.AgenticWorkflowRequest;
import tech.kayys.wayang.engine.BusinessWorkflowRequest;
import tech.kayys.wayang.engine.EscalationRequest;
import tech.kayys.wayang.engine.IntegrationWorkflowRequest;
import tech.kayys.wayang.engine.MetricsRequest;
import tech.kayys.wayang.engine.RetryWorkflowRequest;
import tech.kayys.wayang.engine.UnifiedWorkflowRequest;
import tech.kayys.wayang.engine.WorkflowRunQuery;
import tech.kayys.wayang.sdk.dto.CancelWorkflowRequest;
import tech.kayys.wayang.engine.WorkflowEventRequest;
import tech.kayys.wayang.engine.WorkflowExecutionEvent;
import tech.kayys.wayang.service.WorkflowEngineService;
import tech.kayys.wayang.node.dto.ErrorResponse;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

/**
 * Unified Workflow Engine REST API.
 * 
 * Supports three workflow execution patterns:
 * 1. Agentic Workflow - AI-driven decision making with agent orchestration
 * 2. Integration Workflow - System-to-system data transformation and routing
 * 3. Business Automation Workflow - Human-in-the-loop process automation
 * 
 * Architecture Principles:
 * - Engine maintains sovereignty over execution state
 * - All workflows use deterministic state machine
 * - Error handling is first-class with error output ports
 * - HITL suspension/resumption fully supported
 * - Multi-tenant isolation enforced at API level
 * 
 * @since 1.0.0
 */
@Path("/api/v1/engine")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Workflow Engine", description = "Unified workflow execution and management")
public class WorkflowEngineResource {

        private static final Logger LOG = Logger.getLogger(WorkflowEngineResource.class);

        @Inject
        WorkflowEngineService engineService;

        @Context
        SecurityContext securityContext;

        // ========================================================================
        // UNIFIED WORKFLOW EXECUTION
        // ========================================================================

        @POST
        @Path("/execute")
        @Operation(summary = "Execute workflow", description = "Execute any workflow type with unified request model")
        public Uni<Response> executeWorkflow(UnifiedWorkflowRequest request) {
                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Executing workflow: %s, type: %s, tenant: %s, user: %s",
                                request.getWorkflowId(), request.getWorkflowType(), tenantId, userId);

                // Set execution context
                request.setTenantId(tenantId);
                request.setTriggeredBy(userId);

                return engineService.executeWorkflow(request)
                                .map(response -> Response
                                                .status(Response.Status.ACCEPTED)
                                                .entity(response)
                                                .build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to execute workflow", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/execute/sync")
        @Operation(summary = "Execute workflow synchronously", description = "Blocks until workflow completes or timeout is reached")
        public Uni<Response> executeWorkflowSync(
                        UnifiedWorkflowRequest request,
                        @QueryParam("timeout") @DefaultValue("60000") long timeoutMs) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Executing workflow synchronously: %s, timeout: %dms",
                                request.getWorkflowId(), timeoutMs);

                request.setTenantId(tenantId);
                request.setTriggeredBy(userId);

                return engineService.executeWorkflowSync(request, timeoutMs)
                                .map(response -> Response.ok(response).build())
                                .onFailure().recoverWithItem(th -> {
                                        if (th instanceof TimeoutException) {
                                                return Response.status(Response.Status.REQUEST_TIMEOUT)
                                                                .entity(ErrorResponse
                                                                                .timeout("Workflow execution timeout"))
                                                                .build();
                                        }
                                        LOG.error("Failed to execute workflow sync", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // AGENTIC WORKFLOW EXECUTION
        // ========================================================================

        @POST
        @Path("/agentic")
        @Operation(summary = "Execute agentic workflow", description = "Execute workflow with AI-driven decision making")
        public Uni<Response> executeAgenticWorkflow(AgenticWorkflowRequest request) {
                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Executing agentic workflow: %s, agent: %s",
                                request.getWorkflowId(), request.getAgentConfig().getPrimaryAgent());

                request.setTenantId(tenantId);
                request.setTriggeredBy(userId);

                return engineService.executeAgenticWorkflow(request)
                                .map(response -> Response
                                                .status(Response.Status.ACCEPTED)
                                                .entity(response)
                                                .build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to execute agentic workflow", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/agentic/stream")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        @Operation(summary = "Execute agentic workflow with streaming", description = "Stream execution events and agent decisions")
        public Multi<AgentExecutionEvent> executeAgenticWorkflowStream(
                        AgenticWorkflowRequest request) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Executing agentic workflow with streaming: %s",
                                request.getWorkflowId());

                request.setTenantId(tenantId);
                request.setTriggeredBy(userId);

                return engineService.executeAgenticWorkflowStream(request)
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to execute agentic workflow stream", th);
                                        return AgentExecutionEvent.error(th.getMessage());
                                });
        }

        // ========================================================================
        // INTEGRATION WORKFLOW EXECUTION
        // ========================================================================

        @POST
        @Path("/integration")
        @Operation(summary = "Execute integration workflow", description = "Execute system-to-system data flow workflow")
        public Uni<Response> executeIntegrationWorkflow(
                        IntegrationWorkflowRequest request) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Executing integration workflow: %s, source: %s, target: %s",
                                request.getWorkflowId(),
                                request.getIntegrationConfig().getSourceConnector(),
                                request.getIntegrationConfig().getTargetConnector());

                request.setTenantId(tenantId);
                request.setTriggeredBy(userId);

                return engineService.executeIntegrationWorkflow(request)
                                .map(response -> Response
                                                .status(Response.Status.ACCEPTED)
                                                .entity(response)
                                                .build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to execute integration workflow", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/integration/batch")
        @Operation(summary = "Execute integration workflow in batch", description = "Process large datasets in parallel chunks")
        public Uni<Response> executeIntegrationWorkflowBatch(
                        IntegrationWorkflowRequest request) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Executing integration workflow batch: %s, batchSize: %d",
                                request.getWorkflowId(), request.getBatchSize());

                request.setTenantId(tenantId);
                request.setTriggeredBy(userId);

                return engineService.executeIntegrationWorkflowBatch(request)
                                .map(response -> Response
                                                .status(Response.Status.ACCEPTED)
                                                .entity(response)
                                                .build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to execute integration workflow batch", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // BUSINESS AUTOMATION WORKFLOW EXECUTION
        // ========================================================================

        @POST
        @Path("/business")
        @Operation(summary = "Execute business automation workflow", description = "Execute workflow with human approval steps")
        public Uni<Response> executeBusinessWorkflow(BusinessWorkflowRequest request) {
                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Executing business workflow: %s, approvers: %s",
                                request.getWorkflowId(),
                                request.getBusinessConfig().getApprovalChain());

                request.setTenantId(tenantId);
                request.setTriggeredBy(userId);

                return engineService.executeBusinessWorkflow(request)
                                .map(response -> Response
                                                .status(Response.Status.ACCEPTED)
                                                .entity(response)
                                                .build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to execute business workflow", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @GET
        @Path("/business/{runId}")
        @Operation(summary = "Get business workflow details", description = "Get workflow details with HITL status")
        public Uni<Response> getBusinessWorkflowDetails(
                        @PathParam("runId") String runId) {

                String tenantId = getTenantId();
                LOG.infof("Fetching business workflow details: %s", runId);

                return engineService.getBusinessWorkflowDetails(runId, tenantId)
                                .map(details -> Response.ok(details).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch business workflow details", th);
                                        return Response.status(Response.Status.NOT_FOUND)
                                                        .entity(ErrorResponse.notFound("Workflow run not found"))
                                                        .build();
                                });
        }

        // ========================================================================
        // WORKFLOW STATE MANAGEMENT
        // ========================================================================

        @GET
        @Path("/{runId}/state")
        @Operation(summary = "Get workflow state")
        public Uni<Response> getWorkflowState(@PathParam("runId") String runId) {
                String tenantId = getTenantId();
                LOG.infof("Fetching workflow state: %s", runId);

                return engineService.getWorkflowState(runId, tenantId)
                                .map(state -> Response.ok(state).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch workflow state", th);
                                        return Response.status(Response.Status.NOT_FOUND)
                                                        .entity(ErrorResponse.notFound("Workflow run not found"))
                                                        .build();
                                });
        }

        @GET
        @Path("/{runId}/history")
        @Operation(summary = "Get execution history")
        public Uni<Response> getExecutionHistory(@PathParam("runId") String runId) {
                String tenantId = getTenantId();
                LOG.infof("Fetching execution history: %s", runId);

                return engineService.getExecutionHistory(runId, tenantId)
                                .map(history -> Response.ok(history).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch execution history", th);
                                        return Response.status(Response.Status.NOT_FOUND)
                                                        .entity(ErrorResponse.notFound("Workflow run not found"))
                                                        .build();
                                });
        }

        @GET
        @Path("/{runId}/plan")
        @Operation(summary = "Get execution plan")
        public Uni<Response> getExecutionPlan(@PathParam("runId") String runId) {
                String tenantId = getTenantId();
                LOG.infof("Fetching execution plan: %s", runId);

                return engineService.getExecutionPlan(runId, tenantId)
                                .map(plan -> Response.ok(plan).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch execution plan", th);
                                        return Response.status(Response.Status.NOT_FOUND)
                                                        .entity(ErrorResponse.notFound("Execution plan not found"))
                                                        .build();
                                });
        }

        // ========================================================================
        // WORKFLOW CONTROL OPERATIONS
        // ========================================================================

        @POST
        @Path("/{runId}/pause")
        @Operation(summary = "Pause workflow")
        public Uni<Response> pauseWorkflow(
                        @PathParam("runId") String runId,
                        @QueryParam("reason") String reason) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Pausing workflow: %s, reason: %s, user: %s",
                                runId, reason, userId);

                return engineService.pauseWorkflow(runId, reason, tenantId, userId)
                                .map(response -> Response.ok(response).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to pause workflow", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/{runId}/resume")
        @Operation(summary = "Resume workflow")
        public Uni<Response> resumeWorkflow(@PathParam("runId") String runId) {
                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Resuming workflow: %s, user: %s", runId, userId);

                return engineService.resumeWorkflow(runId, tenantId, userId)
                                .map(response -> Response.ok(response).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to resume workflow", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/{runId}/cancel")
        @Operation(summary = "Cancel workflow")
        public Uni<Response> cancelWorkflow(
                        @PathParam("runId") String runId,
                        CancelWorkflowRequest request) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Cancelling workflow: %s, reason: %s, user: %s",
                                runId, request.getReason(), userId);

                return engineService.cancelWorkflow(runId, request, tenantId, userId)
                                .map(response -> Response.ok(response).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to cancel workflow", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/{runId}/retry")
        @Operation(summary = "Retry workflow")
        public Uni<Response> retryWorkflow(
                        @PathParam("runId") String runId,
                        RetryWorkflowRequest request) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Retrying workflow: %s, user: %s", runId, userId);

                return engineService.retryWorkflow(runId, request, tenantId, userId)
                                .map(response -> Response.ok(response).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to retry workflow", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // ERROR HANDLING AND RECOVERY
        // ========================================================================

        @GET
        @Path("/{runId}/errors")
        @Operation(summary = "Get workflow errors")
        public Uni<Response> getWorkflowErrors(@PathParam("runId") String runId) {
                String tenantId = getTenantId();
                LOG.infof("Fetching workflow errors: %s", runId);

                return engineService.getWorkflowErrors(runId, tenantId)
                                .map(errors -> Response.ok(errors).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch workflow errors", th);
                                        return Response.status(Response.Status.NOT_FOUND)
                                                        .entity(ErrorResponse.notFound("Workflow run not found"))
                                                        .build();
                                });
        }

        @POST
        @Path("/{runId}/nodes/{nodeId}/heal")
        @Operation(summary = "Trigger self-healing")
        public Uni<Response> triggerSelfHealing(
                        @PathParam("runId") String runId,
                        @PathParam("nodeId") String nodeId) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Triggering self-healing: run=%s, node=%s, user=%s",
                                runId, nodeId, userId);

                return engineService.triggerSelfHealing(runId, nodeId, tenantId, userId)
                                .map(response -> Response.ok(response).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to trigger self-healing", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/{runId}/nodes/{nodeId}/escalate")
        @Operation(summary = "Escalate to human")
        public Uni<Response> escalateToHuman(
                        @PathParam("runId") String runId,
                        @PathParam("nodeId") String nodeId,
                        EscalationRequest request) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Escalating to human: run=%s, node=%s, user=%s",
                                runId, nodeId, userId);

                return engineService.escalateToHuman(runId, nodeId, request, tenantId, userId)
                                .map(response -> Response.ok(response).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to escalate to human", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // STREAMING AND EVENTS
        // ========================================================================

        @GET
        @Path("/{runId}/stream")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        @Operation(summary = "Stream execution events")
        public Multi<WorkflowExecutionEvent> streamExecution(
                        @PathParam("runId") String runId) {

                String tenantId = getTenantId();
                LOG.infof("Streaming execution events: %s", runId);

                return engineService.streamExecution(runId, tenantId)
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to stream execution", th);
                                        return WorkflowExecutionEvent.error(th.getMessage());
                                });
        }

        @POST
        @Path("/{runId}/events")
        @Operation(summary = "Inject external event")
        public Uni<Response> injectEvent(
                        @PathParam("runId") String runId,
                        WorkflowEventRequest event) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Injecting event: run=%s, type=%s, user=%s",
                                runId, event.getEventType(), userId);

                return engineService.injectEvent(runId, event, tenantId, userId)
                                .map(v -> Response.noContent().build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to inject event", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // WORKFLOW OUTPUT AND VARIABLES
        // ========================================================================

        @GET
        @Path("/{runId}/output")
        @Operation(summary = "Get workflow output")
        public Uni<Response> getWorkflowOutput(@PathParam("runId") String runId) {
                String tenantId = getTenantId();
                LOG.infof("Fetching workflow output: %s", runId);

                return engineService.getWorkflowOutput(runId, tenantId)
                                .map(output -> Response.ok(output).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch workflow output", th);
                                        return Response.status(Response.Status.NOT_FOUND)
                                                        .entity(ErrorResponse.notFound(
                                                                        "Workflow run not found or not completed"))
                                                        .build();
                                });
        }

        @PATCH
        @Path("/{runId}/variables")
        @Operation(summary = "Update workflow variables")
        public Uni<Response> updateWorkflowVariables(
                        @PathParam("runId") String runId,
                        Map<String, Object> variables) {

                String tenantId = getTenantId();
                String userId = getUserId();

                LOG.infof("Updating workflow variables: run=%s, user=%s", runId, userId);

                return engineService.updateWorkflowVariables(runId, variables, tenantId, userId)
                                .map(v -> Response.noContent().build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to update workflow variables", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // QUERY AND ANALYTICS
        // ========================================================================

        @POST
        @Path("/query")
        @Operation(summary = "Query workflow runs")
        public Uni<Response> queryWorkflowRuns(WorkflowRunQuery query) {
                String tenantId = getTenantId();
                query.setTenantId(tenantId);

                LOG.infof("Querying workflow runs: tenant=%s", tenantId);

                return engineService.queryWorkflowRuns(query)
                                .map(results -> Response.ok(results).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to query workflow runs", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/metrics/{workflowId}")
        @Operation(summary = "Get workflow metrics")
        public Uni<Response> getWorkflowMetrics(
                        @PathParam("workflowId") String workflowId,
                        MetricsRequest request) {

                String tenantId = getTenantId();
                LOG.infof("Fetching workflow metrics: workflow=%s", workflowId);

                return engineService.getWorkflowMetrics(workflowId, request, tenantId)
                                .map(metrics -> Response.ok(metrics).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to fetch workflow metrics", th);
                                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // SIMULATION AND TESTING
        // ========================================================================

        @POST
        @Path("/simulate")
        @Operation(summary = "Simulate workflow execution")
        public Uni<Response> simulateWorkflow(UnifiedWorkflowRequest request) {
                String tenantId = getTenantId();
                request.setTenantId(tenantId);

                LOG.infof("Simulating workflow: %s", request.getWorkflowId());

                return engineService.simulateWorkflow(request)
                                .map(simulation -> Response.ok(simulation).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to simulate workflow", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        @POST
        @Path("/validate")
        @Operation(summary = "Validate workflow execution")
        public Uni<Response> validateWorkflowExecution(
                        UnifiedWorkflowRequest request) {

                String tenantId = getTenantId();
                request.setTenantId(tenantId);

                LOG.infof("Validating workflow: %s", request.getWorkflowId());

                return engineService.validateWorkflowExecution(request)
                                .map(validation -> Response.ok(validation).build())
                                .onFailure().recoverWithItem(th -> {
                                        LOG.error("Failed to validate workflow", th);
                                        return Response.status(Response.Status.BAD_REQUEST)
                                                        .entity(ErrorResponse.from(th))
                                                        .build();
                                });
        }

        // ========================================================================
        // HELPER METHODS
        // ========================================================================

        private String getTenantId() {
                if (securityContext.getUserPrincipal() != null) {
                        String name = securityContext.getUserPrincipal().getName();
                        if (name.contains("@")) {
                                return name.split("@")[1];
                        }
                }
                return "default-tenant";
        }

        private String getUserId() {
                if (securityContext.getUserPrincipal() != null) {
                        return securityContext.getUserPrincipal().getName();
                }
                return "system";
        }
}
