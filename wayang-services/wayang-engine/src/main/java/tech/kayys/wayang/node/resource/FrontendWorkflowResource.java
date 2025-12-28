package tech.kayys.wayang.node.resource;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import tech.kayys.wayang.engine.ErrorPayloadResponse;
import tech.kayys.wayang.engine.EscalationRequest;
import tech.kayys.wayang.engine.NodeExecutionRecord;
import tech.kayys.wayang.engine.RetryWorkflowRequest;
import tech.kayys.wayang.engine.SelfHealingResponse;
import tech.kayys.wayang.engine.WorkflowMetricsResponse;
import tech.kayys.wayang.sdk.dto.CancelWorkflowRequest;
import tech.kayys.wayang.sdk.dto.ValidationResponse;
import tech.kayys.wayang.sdk.dto.WorkflowDefinitionResponse;
import tech.kayys.wayang.engine.WorkflowExecutionEvent;
import tech.kayys.wayang.sdk.dto.WorkflowRunResponse;
import tech.kayys.wayang.sdk.dto.htil.HumanTaskResponse;
import tech.kayys.wayang.sdk.dto.htil.TaskCommentRequest;
import tech.kayys.wayang.sdk.dto.htil.TaskCompletionRequest;
import tech.kayys.wayang.service.WorkflowExecutionService;
import tech.kayys.wayang.service.WorkflowDesignerService;
import tech.kayys.wayang.node.service.NodeTypeService;
import tech.kayys.wayang.node.dto.NodeTypeCatalogResponse;
import tech.kayys.wayang.node.dto.NodeTypeDescriptor;
import tech.kayys.wayang.node.dto.PagedResponse;
import tech.kayys.wayang.schema.WorkflowDesignRequest;
import tech.kayys.wayang.schema.WorkflowExecutionRequest;
import tech.kayys.wayang.schema.WorkflowRunDetailResponse;
import tech.kayys.wayang.schema.WorkflowRunSummary;
import tech.kayys.wayang.schema.DashboardStatsResponse;
import tech.kayys.wayang.node.dto.NodeSuggestionRequest;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

/**
 * Frontend API for Wayang Workflow Platform.
 * 
 * Provides comprehensive REST endpoints for:
 * 1. Node Type Discovery - Get all available built-in nodes
 * 2. Workflow Design - Create, update, validate workflows
 * 3. Workflow Execution - Trigger and monitor workflows
 * 4. Error Handling - Manage failures and recoveries
 * 5. HITL Management - Human task operations
 * 
 * Architecture:
 * - Multi-tenant isolated
 * - Role-based access control
 * - Audit logging for all operations
 * - Reactive non-blocking I/O
 * 
 * @since 1.0.0
 */
@Path("/api/v1/frontend")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Frontend API", description = "API for workflow designer and execution UI")
public class FrontendWorkflowResource {

    private static final Logger LOG = Logger.getLogger(FrontendWorkflowResource.class);

    @Inject
    NodeTypeService nodeTypeService;

    @Inject
    WorkflowDesignerService designerService;

    @Inject
    WorkflowExecutionService executionService;

    @Context
    SecurityContext securityContext;

    // ========================================================================
    // NODE TYPE DISCOVERY
    // ========================================================================

    /**
     * Get all available built-in node types.
     * Used by workflow designer UI to show available nodes in palette.
     * 
     * @return List of node type descriptors grouped by category
     */
    @GET
    @Path("/node-types")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get all built-in node types", description = "Returns all available node types organized by category for the workflow designer palette")
    public Uni<NodeTypeCatalogResponse> getNodeTypes() {
        String tenantId = getCurrentTenantId();
        LOG.infof("Fetching node types for tenant: %s", tenantId);

        return nodeTypeService.getNodeTypeCatalog(tenantId)
                .invoke(catalog -> LOG.infof("Retrieved %d node types", catalog.getTotalNodes()));
    }

    /**
     * Get detailed information about a specific node type.
     * 
     * @param nodeTypeId Node type identifier
     * @return Detailed node type descriptor with I/O schema
     */
    @GET
    @Path("/node-types/{nodeTypeId}")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get node type details")
    public Uni<NodeTypeDescriptor> getNodeTypeDetails(
            @PathParam("nodeTypeId") String nodeTypeId) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Fetching node type details: %s for tenant: %s", nodeTypeId, tenantId);

        return nodeTypeService.getNodeTypeDescriptor(nodeTypeId, tenantId);
    }

    /**
     * Search node types by keyword.
     * 
     * @param query    Search query
     * @param category Optional category filter
     * @return Matching node types
     */
    @GET
    @Path("/node-types/search")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Search node types")
    public Uni<List<NodeTypeDescriptor>> searchNodeTypes(
            @QueryParam("q") @NotNull String query,
            @QueryParam("category") String category) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Searching node types: query=%s, category=%s, tenant=%s",
                query, category, tenantId);

        return nodeTypeService.searchNodeTypes(query, category, tenantId);
    }

    // ========================================================================
    // WORKFLOW DESIGN
    // ========================================================================

    /**
     * Create a new workflow definition.
     * 
     * @param request Workflow creation request
     * @return Created workflow with generated ID
     */
    @POST
    @Path("/workflows")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Create workflow", description = "Create a new workflow definition in draft state")
    public Uni<WorkflowDefinitionResponse> createWorkflow(
            @Valid WorkflowDesignRequest request) {
        String tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        LOG.infof("Creating workflow: %s for tenant: %s by user: %s",
                request.getName(), tenantId, userId);

        return designerService.createWorkflow(request, tenantId, userId)
                .invoke(workflow -> LOG.infof("Workflow created: %s", workflow.getId()));
    }

    /**
     * Update existing workflow definition.
     * 
     * @param workflowId Workflow identifier
     * @param request    Updated workflow definition
     * @return Updated workflow
     */
    @PUT
    @Path("/workflows/{workflowId}")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Update workflow")
    public Uni<WorkflowDefinitionResponse> updateWorkflow(
            @PathParam("workflowId") String workflowId,
            @Valid WorkflowDesignRequest request) {
        String tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        LOG.infof("Updating workflow: %s for tenant: %s", workflowId, tenantId);

        return designerService.updateWorkflow(workflowId, request, tenantId, userId);
    }

    /**
     * Get workflow definition by ID.
     * 
     * @param workflowId Workflow identifier
     * @return Workflow definition
     */
    @GET
    @Path("/workflows/{workflowId}")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get workflow definition")
    public Uni<WorkflowDefinitionResponse> getWorkflow(
            @PathParam("workflowId") String workflowId) {
        String tenantId = getCurrentTenantId();
        return designerService.getWorkflow(workflowId, tenantId);
    }

    /**
     * List all workflows for current tenant.
     * 
     * @param page   Page number (0-indexed)
     * @param size   Page size
     * @param status Filter by status (draft, published, archived)
     * @param tags   Filter by tags
     * @return Paginated workflow list
     */
    @GET
    @Path("/workflows")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "List workflows")
    public Uni<PagedResponse<WorkflowDefinitionResponse>> listWorkflows(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size,
            @QueryParam("status") String status,
            @QueryParam("tags") List<String> tags) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Listing workflows for tenant: %s (page=%d, size=%d)",
                tenantId, page, size);

        return designerService.listWorkflows(tenantId, page, size, status, tags);
    }

    /**
     * Validate workflow definition without saving.
     * 
     * @param request Workflow to validate
     * @return Validation result with errors/warnings
     */
    @POST
    @Path("/workflows/validate")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Validate workflow", description = "Validate workflow definition including graph structure, port compatibility, and policy compliance")
    public Uni<ValidationResponse> validateWorkflow(
            @Valid WorkflowDesignRequest request) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Validating workflow: %s", request.getName());

        return designerService.validateWorkflow(request, tenantId);
    }

    /**
     * Publish workflow to make it executable.
     * 
     * @param workflowId Workflow identifier
     * @param version    Version to publish (e.g., "1.0.0")
     * @return Published workflow
     */
    @POST
    @Path("/workflows/{workflowId}/publish")
    @RolesAllowed({ "admin" })
    @Operation(summary = "Publish workflow")
    public Uni<WorkflowDefinitionResponse> publishWorkflow(
            @PathParam("workflowId") String workflowId,
            @QueryParam("version") @NotNull String version) {
        String tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        LOG.infof("Publishing workflow: %s version: %s", workflowId, version);

        return designerService.publishWorkflow(workflowId, version, tenantId, userId);
    }

    /**
     * Delete workflow definition.
     * 
     * @param workflowId Workflow identifier
     * @return No content on success
     */
    @DELETE
    @Path("/workflows/{workflowId}")
    @RolesAllowed({ "admin" })
    @Operation(summary = "Delete workflow")
    public Uni<Response> deleteWorkflow(
            @PathParam("workflowId") String workflowId) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Deleting workflow: %s for tenant: %s", workflowId, tenantId);

        return designerService.deleteWorkflow(workflowId, tenantId)
                .map(v -> Response.noContent().build());
    }

    // ========================================================================
    // WORKFLOW EXECUTION
    // ========================================================================

    /**
     * Trigger workflow execution.
     * Supports all three workflow types: agentic, integration, business.
     * 
     * @param workflowId Workflow identifier
     * @param request    Execution request with inputs
     * @return Workflow run response with run ID
     */
    @POST
    @Path("/workflows/{workflowId}/execute")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Execute workflow", description = "Trigger workflow execution with provided inputs")
    public Uni<WorkflowRunResponse> executeWorkflow(
            @PathParam("workflowId") String workflowId,
            @Valid WorkflowExecutionRequest request) {
        String tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        LOG.infof("Executing workflow: %s for tenant: %s by user: %s",
                workflowId, tenantId, userId);

        return executionService.executeWorkflow(workflowId, request, tenantId, userId)
                .invoke(run -> LOG.infof("Workflow started: runId=%s", run.getRunId()));
    }

    /**
     * Get workflow run status.
     * 
     * @param runId Workflow run identifier
     * @return Current run status and output
     */
    @GET
    @Path("/runs/{runId}")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get workflow run status")
    public Uni<WorkflowRunDetailResponse> getWorkflowRun(
            @PathParam("runId") String runId) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Fetching workflow run: %s for tenant: %s", runId, tenantId);

        return executionService.getWorkflowRunDetails(runId, tenantId);
    }

    /**
     * Get workflow execution history.
     * 
     * @param runId Workflow run identifier
     * @return Ordered list of node executions
     */
    @GET
    @Path("/runs/{runId}/history")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get execution history")
    public Uni<List<NodeExecutionRecord>> getExecutionHistory(
            @PathParam("runId") String runId) {
        String tenantId = getCurrentTenantId();
        return executionService.getExecutionHistory(runId, tenantId);
    }

    /**
     * Stream workflow execution events in real-time.
     * 
     * @param runId Workflow run identifier
     * @return SSE stream of execution events
     */
    @GET
    @Path("/runs/{runId}/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Stream execution events", description = "Subscribe to real-time workflow execution events via SSE")
    public Multi<WorkflowExecutionEvent> streamExecution(
            @PathParam("runId") String runId) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Streaming execution for run: %s", runId);

        return executionService.streamExecution(runId, tenantId);
    }

    /**
     * List workflow runs with filtering.
     * 
     * @param workflowId Optional workflow filter
     * @param status     Optional status filter
     * @param page       Page number
     * @param size       Page size
     * @return Paginated workflow runs
     */
    @GET
    @Path("/runs")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "List workflow runs")
    public Uni<PagedResponse<WorkflowRunSummary>> listWorkflowRuns(
            @QueryParam("workflowId") String workflowId,
            @QueryParam("status") String status,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Listing workflow runs for tenant: %s", tenantId);

        return executionService.listWorkflowRuns(
                tenantId, workflowId, status, page, size);
    }

    // ========================================================================
    // WORKFLOW CONTROL
    // ========================================================================

    /**
     * Pause running workflow.
     * 
     * @param runId  Workflow run identifier
     * @param reason Reason for pausing
     * @return Updated workflow run
     */
    @POST
    @Path("/runs/{runId}/pause")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Pause workflow")
    public Uni<WorkflowRunResponse> pauseWorkflow(
            @PathParam("runId") String runId,
            @QueryParam("reason") String reason) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Pausing workflow run: %s, reason: %s", runId, reason);

        return executionService.pauseWorkflow(runId, reason, tenantId);
    }

    /**
     * Resume paused workflow.
     * 
     * @param runId Workflow run identifier
     * @return Updated workflow run
     */
    @POST
    @Path("/runs/{runId}/resume")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Resume workflow")
    public Uni<WorkflowRunResponse> resumeWorkflow(
            @PathParam("runId") String runId) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Resuming workflow run: %s", runId);

        return executionService.resumeWorkflow(runId, tenantId);
    }

    /**
     * Cancel running workflow.
     * 
     * @param runId   Workflow run identifier
     * @param request Cancellation request with reason
     * @return Updated workflow run
     */
    @POST
    @Path("/runs/{runId}/cancel")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Cancel workflow")
    public Uni<WorkflowRunResponse> cancelWorkflow(
            @PathParam("runId") String runId,
            @Valid CancelWorkflowRequest request) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Cancelling workflow run: %s", runId);

        return executionService.cancelWorkflow(runId, request, tenantId);
    }

    /**
     * Retry failed workflow.
     * 
     * @param runId   Failed workflow run identifier
     * @param request Retry configuration
     * @return New workflow run
     */
    @POST
    @Path("/runs/{runId}/retry")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Retry workflow")
    public Uni<WorkflowRunResponse> retryWorkflow(
            @PathParam("runId") String runId,
            @Valid RetryWorkflowRequest request) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Retrying workflow run: %s", runId);

        return executionService.retryWorkflow(runId, request, tenantId);
    }

    // ========================================================================
    // ERROR HANDLING
    // ========================================================================

    /**
     * Get errors for failed workflow run.
     * 
     * @param runId Workflow run identifier
     * @return List of error payloads
     */
    @GET
    @Path("/runs/{runId}/errors")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get workflow errors")
    public Uni<List<ErrorPayloadResponse>> getWorkflowErrors(
            @PathParam("runId") String runId) {
        String tenantId = getCurrentTenantId();
        return executionService.getWorkflowErrors(runId, tenantId);
    }

    /**
     * Trigger self-healing for failed node.
     * 
     * @param runId  Workflow run identifier
     * @param nodeId Failed node identifier
     * @return Self-healing result
     */
    @POST
    @Path("/runs/{runId}/nodes/{nodeId}/heal")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Trigger self-healing", description = "Use LLM to analyze error and generate corrected input")
    public Uni<SelfHealingResponse> triggerSelfHealing(
            @PathParam("runId") String runId,
            @PathParam("nodeId") String nodeId) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Triggering self-healing for run: %s, node: %s", runId, nodeId);

        return executionService.triggerSelfHealing(runId, nodeId, tenantId);
    }

    /**
     * Escalate error to human operator.
     * 
     * @param runId   Workflow run identifier
     * @param nodeId  Failed node identifier
     * @param request Escalation details
     * @return Created human task
     */
    @POST
    @Path("/runs/{runId}/nodes/{nodeId}/escalate")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Escalate to human")
    public Uni<HumanTaskResponse> escalateToHuman(
            @PathParam("runId") String runId,
            @PathParam("nodeId") String nodeId,
            @Valid EscalationRequest request) {
        String tenantId = getCurrentTenantId();
        String userId = getCurrentUserId();
        LOG.infof("Escalating error to human: run=%s, node=%s", runId, nodeId);

        return executionService.escalateToHuman(runId, nodeId, request, tenantId, userId);
    }

    // ========================================================================
    // HUMAN TASKS (HITL)
    // ========================================================================

    /**
     * Get pending human tasks for current user.
     * 
     * @param priority Optional priority filter
     * @return List of pending tasks
     */
    @GET
    @Path("/tasks/pending")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get pending tasks")
    public Uni<List<HumanTaskResponse>> getPendingTasks(
            @QueryParam("priority") String priority) {
        String userId = getCurrentUserId();
        LOG.infof("Fetching pending tasks for user: %s", userId);

        return executionService.getPendingTasksForUser(userId, priority);
    }

    /**
     * Get human task details.
     * 
     * @param taskId Task identifier
     * @return Task details with context
     */
    @GET
    @Path("/tasks/{taskId}")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get task details")
    public Uni<HumanTaskResponse> getTask(
            @PathParam("taskId") String taskId) {
        String userId = getCurrentUserId();
        return executionService.getTaskDetails(taskId, userId);
    }

    /**
     * Complete human task with decision.
     * 
     * @param taskId  Task identifier
     * @param request Task completion details
     * @return No content on success
     */
    @POST
    @Path("/tasks/{taskId}/complete")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Complete task")
    public Uni<Response> completeTask(
            @PathParam("taskId") String taskId,
            @Valid TaskCompletionRequest request) {
        String userId = getCurrentUserId();
        LOG.infof("Completing task: %s by user: %s", taskId, userId);

        return executionService.completeTask(taskId, request, userId)
                .map(v -> Response.noContent().build());
    }

    /**
     * Add comment to human task.
     * 
     * @param taskId  Task identifier
     * @param comment Comment request
     * @return No content on success
     */
    @POST
    @Path("/tasks/{taskId}/comments")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Add task comment")
    public Uni<Response> addTaskComment(
            @PathParam("taskId") String taskId,
            @Valid TaskCommentRequest comment) {
        String userId = getCurrentUserId();
        comment.setUserId(userId);

        return executionService.addTaskComment(taskId, comment)
                .map(v -> Response.noContent().build());
    }

    // ========================================================================
    // METRICS AND ANALYTICS
    // ========================================================================

    /**
     * Get workflow execution metrics.
     * 
     * @param workflowId Workflow identifier
     * @param days       Number of days to analyze (default: 30)
     * @return Workflow metrics
     */
    @GET
    @Path("/workflows/{workflowId}/metrics")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get workflow metrics")
    public Uni<WorkflowMetricsResponse> getWorkflowMetrics(
            @PathParam("workflowId") String workflowId,
            @QueryParam("days") @DefaultValue("30") int days) {
        String tenantId = getCurrentTenantId();
        LOG.infof("Fetching metrics for workflow: %s, days: %d", workflowId, days);

        return executionService.getWorkflowMetrics(workflowId, days, tenantId);
    }

    /**
     * Get dashboard statistics for tenant.
     * 
     * @return Dashboard statistics
     */
    @GET
    @Path("/dashboard/stats")
    @RolesAllowed({ "user", "admin" })
    @Operation(summary = "Get dashboard statistics")
    public Uni<DashboardStatsResponse> getDashboardStats() {
        String tenantId = getCurrentTenantId();
        LOG.infof("Fetching dashboard stats for tenant: %s", tenantId);

        return executionService.getDashboardStats(tenantId);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private String getCurrentTenantId() {
        if (securityContext.getUserPrincipal() != null) {
            String name = securityContext.getUserPrincipal().getName();
            if (name.contains("@")) {
                return name.split("@")[1];
            }
        }
        return "default-tenant";
    }

    private String getCurrentUserId() {
        if (securityContext.getUserPrincipal() != null) {
            return securityContext.getUserPrincipal().getName();
        }
        return "system";
    }
}
