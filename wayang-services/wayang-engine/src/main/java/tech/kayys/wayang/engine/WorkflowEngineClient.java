package tech.kayys.wayang.engine;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import tech.kayys.wayang.sdk.AgentClient;
import tech.kayys.wayang.sdk.HITLClient;
import tech.kayys.wayang.sdk.WorkflowRunClient;
import tech.kayys.wayang.sdk.dto.CancelWorkflowRequest;
import tech.kayys.wayang.sdk.dto.ValidationResponse;
import tech.kayys.wayang.sdk.dto.WorkflowEventRequest;
import tech.kayys.wayang.sdk.dto.WorkflowExecutionEvent;
import tech.kayys.wayang.sdk.dto.WorkflowRunResponse;
import tech.kayys.wayang.sdk.dto.htil.HumanTaskResponse;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.List;
import java.util.Map;

/**
 * WorkflowEngineClient: Unified client for all workflow execution types.
 * 
 * Supports three workflow execution patterns:
 * 1. Regular Agentic Workflow - AI-driven decision making with agent
 * orchestration
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
 * Error Handling:
 * - Every node execution produces success OR error output
 * - ErrorPayload follows standardized schema
 * - Automatic retry with exponential backoff supported
 * - Human escalation for critical failures
 * - Circuit breaker pattern prevents cascading failures
 * 
 * Observability:
 * - Full provenance tracking for audit compliance
 * - Real-time execution streaming via SSE
 * - Telemetry collection at node and workflow level
 * - Distributed tracing with correlation IDs
 * 
 * Usage Example - Agentic Workflow:
 * 
 * <pre>
 * WorkflowEngineClient client = ...;
 * 
 * AgenticWorkflowRequest request = AgenticWorkflowRequest.builder()
 *     .workflowId("customer-support-bot")
 *     .tenantId("acme-corp")
 *     .triggeredBy("system:api")
 *     .agentConfig(AgentConfig.builder()
 *         .primaryAgent("support-agent")
 *         .orchestrationStrategy("dynamic")
 *         .toolsEnabled(List.of("knowledge-base", "ticket-system"))
 *         .build())
 *     .input("customerQuery", "How do I reset my password?")
 *     .build();
 * 
 * Uni<WorkflowRunResponse> result = client.executeAgenticWorkflow(request);
 * </pre>
 * 
 * Usage Example - Integration Workflow:
 * 
 * <pre>
 * IntegrationWorkflowRequest request = IntegrationWorkflowRequest.builder()
 *                 .workflowId("salesforce-to-erp-sync")
 *                 .tenantId("acme-corp")
 *                 .triggeredBy("scheduler:daily")
 *                 .integrationConfig(IntegrationConfig.builder()
 *                                 .sourceConnector("salesforce-api")
 *                                 .targetConnector("sap-erp")
 *                                 .transformationRules("mapping-v2.yaml")
 *                                 .errorStrategy("dead-letter-queue")
 *                                 .build())
 *                 .batchSize(1000)
 *                 .build();
 * 
 * Uni<WorkflowRunResponse> result = client.executeIntegrationWorkflow(request);
 * </pre>
 * 
 * Usage Example - Business Automation Workflow:
 * 
 * <pre>
 * BusinessWorkflowRequest request = BusinessWorkflowRequest.builder()
 *                 .workflowId("expense-approval")
 *                 .tenantId("acme-corp")
 *                 .triggeredBy("user:john.doe")
 *                 .businessConfig(BusinessConfig.builder()
 *                                 .approvalChain(List.of("manager", "finance", "cfo"))
 *                                 .slaHours(48)
 *                                 .escalationPolicy("auto-approve-on-timeout")
 *                                 .build())
 *                 .input("expenseReport", expenseData)
 *                 .build();
 * 
 * Uni<WorkflowRunResponse> result = client.executeBusinessWorkflow(request);
 * </pre>
 * 
 * @since 1.0.0
 * @see WorkflowRunClient for basic run management
 * @see AgentClient for agent-specific operations
 * @see HITLClient for human task management
 */
@Path("/api/v1/engine")
@RegisterRestClient(configKey = "workflow-engine")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface WorkflowEngineClient {

        // ========================================================================
        // UNIFIED WORKFLOW EXECUTION
        // ========================================================================

        /**
         * Execute any workflow type with unified request model.
         * The engine automatically detects workflow type and applies appropriate
         * execution strategy.
         * 
         * @param request Unified workflow execution request
         * @return Workflow run with initial state
         */
        @POST
        @Path("/execute")
        Uni<WorkflowRunResponse> executeWorkflow(UnifiedWorkflowRequest request);

        /**
         * Execute workflow synchronously with timeout.
         * Blocks until workflow completes or timeout is reached.
         * 
         * @param request   Workflow execution request
         * @param timeoutMs Maximum wait time in milliseconds
         * @return Completed workflow run
         */
        @POST
        @Path("/execute/sync")
        Uni<WorkflowRunResponse> executeWorkflowSync(
                        UnifiedWorkflowRequest request,
                        @QueryParam("timeout") @DefaultValue("60000") long timeoutMs);

        // ========================================================================
        // AGENTIC WORKFLOW EXECUTION
        // ========================================================================

        /**
         * Execute agentic workflow with AI-driven decision making.
         * 
         * Characteristics:
         * - Agent-based orchestration (optional orchestrator agent)
         * - LLM-powered reasoning and tool selection
         * - Dynamic planning and replanning
         * - RAG and memory integration
         * - Self-healing on errors
         * 
         * @param request Agentic workflow request with agent configuration
         * @return Workflow run response
         */
        @POST
        @Path("/agentic")
        Uni<WorkflowRunResponse> executeAgenticWorkflow(AgenticWorkflowRequest request);

        /**
         * Execute agentic workflow with streaming output.
         * Useful for conversational AI, long-running analysis, or incremental results.
         * 
         * @param request Agentic workflow request
         * @return Multi stream of execution events and agent decisions
         */
        @POST
        @Path("/agentic/stream")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        Multi<AgentExecutionEvent> executeAgenticWorkflowStream(AgenticWorkflowRequest request);

        // ========================================================================
        // INTEGRATION WORKFLOW EXECUTION
        // ========================================================================

        /**
         * Execute integration workflow for system-to-system data flow.
         * 
         * Characteristics:
         * - Connector-based (HTTP, GraphQL, MQ, Database, Cloud)
         * - Camel-powered routing and transformation
         * - Batch processing support
         * - Idempotency and exactly-once delivery
         * - Dead letter queue for failed messages
         * 
         * @param request Integration workflow request
         * @return Workflow run response
         */
        @POST
        @Path("/integration")
        Uni<WorkflowRunResponse> executeIntegrationWorkflow(IntegrationWorkflowRequest request);

        /**
         * Execute integration workflow with batch processing.
         * Automatically splits large datasets into chunks for parallel processing.
         * 
         * @param request Integration workflow request with batch configuration
         * @return Batch execution response with per-chunk status
         */
        @POST
        @Path("/integration/batch")
        Uni<BatchWorkflowRunResponse> executeIntegrationWorkflowBatch(
                        IntegrationWorkflowRequest request);

        // ========================================================================
        // BUSINESS AUTOMATION WORKFLOW EXECUTION
        // ========================================================================

        /**
         * Execute business automation workflow with human approval steps.
         * 
         * Characteristics:
         * - Human-in-the-loop (HITL) task management
         * - Approval chain and escalation
         * - SLA tracking and timeout handling
         * - Form-based data collection
         * - Audit trail for compliance
         * 
         * @param request Business workflow request
         * @return Workflow run response (may be in WAITING state for human input)
         */
        @POST
        @Path("/business")
        Uni<WorkflowRunResponse> executeBusinessWorkflow(BusinessWorkflowRequest request);

        /**
         * Get business workflow with human task details.
         * Includes pending approvals, form data, and decision history.
         * 
         * @param runId Workflow run identifier
         * @return Business workflow details with HITL status
         */
        @GET
        @Path("/business/{runId}")
        Uni<BusinessWorkflowDetails> getBusinessWorkflowDetails(
                        @PathParam("runId") String runId);

        // ========================================================================
        // WORKFLOW STATE MANAGEMENT
        // ========================================================================

        /**
         * Get current workflow execution state.
         * 
         * @param runId Workflow run identifier
         * @return Current state with node execution status
         */
        @GET
        @Path("/{runId}/state")
        Uni<WorkflowStateResponse> getWorkflowState(@PathParam("runId") String runId);

        /**
         * Get workflow execution history with all node transitions.
         * 
         * @param runId Workflow run identifier
         * @return Ordered list of node executions
         */
        @GET
        @Path("/{runId}/history")
        Uni<List<NodeExecutionRecord>> getExecutionHistory(@PathParam("runId") String runId);

        /**
         * Get workflow execution plan (for orchestrator workflows).
         * Shows the agent's planned execution path.
         * 
         * @param runId Workflow run identifier
         * @return Execution plan with target nodes and conditions
         */
        @GET
        @Path("/{runId}/plan")
        Uni<ExecutionPlanResponse> getExecutionPlan(@PathParam("runId") String runId);

        // ========================================================================
        // WORKFLOW CONTROL OPERATIONS
        // ========================================================================

        /**
         * Pause running workflow.
         * Workflow state is persisted and can be resumed later.
         * 
         * @param runId  Workflow run identifier
         * @param reason Reason for pausing
         * @return Updated workflow run
         */
        @POST
        @Path("/{runId}/pause")
        Uni<WorkflowRunResponse> pauseWorkflow(
                        @PathParam("runId") String runId,
                        @QueryParam("reason") String reason);

        /**
         * Resume paused workflow.
         * 
         * @param runId Workflow run identifier
         * @return Updated workflow run
         */
        @POST
        @Path("/{runId}/resume")
        Uni<WorkflowRunResponse> resumeWorkflow(@PathParam("runId") String runId);

        /**
         * Cancel running or paused workflow.
         * Triggers compensation logic if defined.
         * 
         * @param runId   Workflow run identifier
         * @param request Cancellation request with reason
         * @return Updated workflow run with CANCELLED status
         */
        @POST
        @Path("/{runId}/cancel")
        Uni<WorkflowRunResponse> cancelWorkflow(
                        @PathParam("runId") String runId,
                        CancelWorkflowRequest request);

        /**
         * Retry failed workflow from last successful checkpoint.
         * 
         * @param runId   Workflow run identifier
         * @param request Retry configuration
         * @return New workflow run (retries create new run ID)
         */
        @POST
        @Path("/{runId}/retry")
        Uni<WorkflowRunResponse> retryWorkflow(
                        @PathParam("runId") String runId,
                        RetryWorkflowRequest request);

        // ========================================================================
        // ERROR HANDLING AND RECOVERY
        // ========================================================================

        /**
         * Get error details for failed workflow.
         * 
         * @param runId Workflow run identifier
         * @return Error payload with failure details
         */
        @GET
        @Path("/{runId}/errors")
        Uni<List<ErrorPayloadResponse>> getWorkflowErrors(@PathParam("runId") String runId);

        /**
         * Trigger self-healing for failed node.
         * Uses LLM to analyze error and generate corrected input.
         * 
         * @param runId  Workflow run identifier
         * @param nodeId Failed node identifier
         * @return Self-healing result
         */
        @POST
        @Path("/{runId}/nodes/{nodeId}/heal")
        Uni<SelfHealingResponse> triggerSelfHealing(
                        @PathParam("runId") String runId,
                        @PathParam("nodeId") String nodeId);

        /**
         * Create human review task for error.
         * Escalates failed node to human operator for manual correction.
         * 
         * @param runId   Workflow run identifier
         * @param nodeId  Failed node identifier
         * @param request Human task creation request
         * @return Created human task
         */
        @POST
        @Path("/{runId}/nodes/{nodeId}/escalate")
        Uni<HumanTaskResponse> escalateToHuman(
                        @PathParam("runId") String runId,
                        @PathParam("nodeId") String nodeId,
                        EscalationRequest request);

        // ========================================================================
        // STREAMING AND EVENTS
        // ========================================================================

        /**
         * Stream workflow execution events in real-time.
         * Returns SSE stream with node transitions, errors, and completions.
         * 
         * @param runId Workflow run identifier
         * @return Multi stream of execution events
         */
        @GET
        @Path("/{runId}/stream")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        Multi<WorkflowExecutionEvent> streamExecution(@PathParam("runId") String runId);

        /**
         * Inject external event into running workflow.
         * Used for event-driven workflows that wait for external signals.
         * 
         * @param runId Workflow run identifier
         * @param event External event payload
         * @return Acknowledgment
         */
        @POST
        @Path("/{runId}/events")
        Uni<Void> injectEvent(
                        @PathParam("runId") String runId,
                        WorkflowEventRequest event);

        // ========================================================================
        // WORKFLOW OUTPUT AND VARIABLES
        // ========================================================================

        /**
         * Get workflow output data.
         * Available only after workflow completion.
         * 
         * @param runId Workflow run identifier
         * @return Output data map
         */
        @GET
        @Path("/{runId}/output")
        Uni<Map<String, Object>> getWorkflowOutput(@PathParam("runId") String runId);

        /**
         * Update workflow variables during execution.
         * Used for dynamic parameter injection.
         * 
         * @param runId     Workflow run identifier
         * @param variables Variables to update
         * @return Acknowledgment
         */
        @PATCH
        @Path("/{runId}/variables")
        Uni<Void> updateWorkflowVariables(
                        @PathParam("runId") String runId,
                        Map<String, Object> variables);

        // ========================================================================
        // QUERY AND ANALYTICS
        // ========================================================================

        /**
         * Query workflow runs with advanced filtering.
         * 
         * @param query Query parameters
         * @return Paginated workflow runs
         */
        @POST
        @Path("/query")
        Uni<PagedWorkflowRunResponse> queryWorkflowRuns(WorkflowRunQuery query);

        /**
         * Get workflow execution metrics.
         * Provides aggregated statistics for monitoring and optimization.
         * 
         * @param workflowId Workflow definition identifier
         * @param request    Metrics request with time range
         * @return Workflow metrics
         */
        @POST
        @Path("/metrics/{workflowId}")
        Uni<WorkflowMetricsResponse> getWorkflowMetrics(
                        @PathParam("workflowId") String workflowId,
                        MetricsRequest request);

        // ========================================================================
        // SIMULATION AND TESTING
        // ========================================================================

        /**
         * Simulate workflow execution without side effects.
         * All connector calls and tool invocations are mocked.
         * 
         * @param request Workflow execution request
         * @return Simulation result with predicted execution path
         */
        @POST
        @Path("/simulate")
        Uni<SimulationResponse> simulateWorkflow(UnifiedWorkflowRequest request);

        /**
         * Dry-run workflow with validation only.
         * Checks node compatibility, policy compliance, and resource availability.
         * 
         * @param request Workflow execution request
         * @return Validation result
         */
        @POST
        @Path("/validate")
        Uni<ValidationResponse> validateWorkflowExecution(UnifiedWorkflowRequest request);
}
