package tech.kayys.wayang.service;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.engine.*;
import tech.kayys.wayang.node.dto.PagedResponse;
import tech.kayys.wayang.schema.WorkflowRunSummary;
import tech.kayys.wayang.sdk.dto.CancelWorkflowRequest;
import tech.kayys.wayang.engine.WorkflowEventRequest;
import tech.kayys.wayang.engine.WorkflowExecutionEvent;

import java.util.List;
import java.util.Map;

/**
 * Workflow Engine Service Interface.
 * 
 * Provides unified workflow execution and management:
 * - Execute workflows (agentic, integration, business)
 * - Control workflow execution (pause, resume, cancel, retry)
 * - Monitor workflow state and history
 * - Handle errors and recovery
 * - Stream execution events
 * 
 * @since 1.0.0
 */
public interface WorkflowEngineService {

        // ========================================================================
        // UNIFIED WORKFLOW EXECUTION
        // ========================================================================

        /**
         * Execute workflow with unified request model.
         * 
         * @param request Unified workflow request
         * @return Workflow run response
         */
        Uni<Object> executeWorkflow(UnifiedWorkflowRequest request);

        /**
         * Execute workflow synchronously.
         * 
         * @param request   Unified workflow request
         * @param timeoutMs Timeout in milliseconds
         * @return Workflow run response
         */
        Uni<Object> executeWorkflowSync(UnifiedWorkflowRequest request, long timeoutMs);

        // ========================================================================
        // AGENTIC WORKFLOW EXECUTION
        // ========================================================================

        /**
         * Execute agentic workflow.
         * 
         * @param request Agentic workflow request
         * @return Workflow run response
         */
        Uni<Object> executeAgenticWorkflow(AgenticWorkflowRequest request);

        /**
         * Execute agentic workflow with streaming.
         * 
         * @param request Agentic workflow request
         * @return Stream of agent execution events
         */
        Multi<AgentExecutionEvent> executeAgenticWorkflowStream(AgenticWorkflowRequest request);

        // ========================================================================
        // INTEGRATION WORKFLOW EXECUTION
        // ========================================================================

        /**
         * Execute integration workflow.
         * 
         * @param request Integration workflow request
         * @return Workflow run response
         */
        Uni<Object> executeIntegrationWorkflow(IntegrationWorkflowRequest request);

        /**
         * Execute integration workflow in batch.
         * 
         * @param request Integration workflow request
         * @return Batch workflow run response
         */
        Uni<Object> executeIntegrationWorkflowBatch(IntegrationWorkflowRequest request);

        // ========================================================================
        // BUSINESS AUTOMATION WORKFLOW EXECUTION
        // ========================================================================

        /**
         * Execute business automation workflow.
         * 
         * @param request Business workflow request
         * @return Workflow run response
         */
        Uni<Object> executeBusinessWorkflow(BusinessWorkflowRequest request);

        /**
         * Get business workflow details.
         * 
         * @param runId    Workflow run identifier
         * @param tenantId Tenant identifier
         * @return Business workflow details
         */
        Uni<Object> getBusinessWorkflowDetails(String runId, String tenantId);

        // ========================================================================
        // WORKFLOW STATE MANAGEMENT
        // ========================================================================

        /**
         * Get workflow state.
         * 
         * @param runId    Workflow run identifier
         * @param tenantId Tenant identifier
         * @return Workflow state
         */
        Uni<WorkflowStateResponse> getWorkflowState(String runId, String tenantId);

        /**
         * Get execution history.
         * 
         * @param runId    Workflow run identifier
         * @param tenantId Tenant identifier
         * @return Execution history
         */
        Uni<List<Object>> getExecutionHistory(String runId, String tenantId);

        /**
         * Get execution plan.
         * 
         * @param runId    Workflow run identifier
         * @param tenantId Tenant identifier
         * @return Execution plan
         */
        Uni<ExecutionPlanResponse> getExecutionPlan(String runId, String tenantId);

        // ========================================================================
        // WORKFLOW CONTROL OPERATIONS
        // ========================================================================

        /**
         * Pause workflow.
         * 
         * @param runId    Workflow run identifier
         * @param reason   Pause reason
         * @param tenantId Tenant identifier
         * @param userId   User identifier
         * @return Updated workflow run
         */
        Uni<Object> pauseWorkflow(String runId, String reason, String tenantId, String userId);

        /**
         * Resume workflow.
         * 
         * @param runId    Workflow run identifier
         * @param tenantId Tenant identifier
         * @param userId   User identifier
         * @return Updated workflow run
         */
        Uni<Object> resumeWorkflow(String runId, String tenantId, String userId);

        /**
         * Cancel workflow.
         * 
         * @param runId    Workflow run identifier
         * @param request  Cancel request
         * @param tenantId Tenant identifier
         * @param userId   User identifier
         * @return Updated workflow run
         */
        Uni<Object> cancelWorkflow(String runId, CancelWorkflowRequest request,
                        String tenantId, String userId);

        /**
         * Retry workflow.
         * 
         * @param runId    Workflow run identifier
         * @param request  Retry request
         * @param tenantId Tenant identifier
         * @param userId   User identifier
         * @return New workflow run
         */
        Uni<Object> retryWorkflow(String runId, RetryWorkflowRequest request,
                        String tenantId, String userId);

        // ========================================================================
        // ERROR HANDLING AND RECOVERY
        // ========================================================================

        /**
         * Get workflow errors.
         * 
         * @param runId    Workflow run identifier
         * @param tenantId Tenant identifier
         * @return List of errors
         */
        Uni<List<ErrorPayloadResponse>> getWorkflowErrors(String runId, String tenantId);

        /**
         * Trigger self-healing.
         * 
         * @param runId    Workflow run identifier
         * @param nodeId   Failed node identifier
         * @param tenantId Tenant identifier
         * @param userId   User identifier
         * @return Self-healing response
         */
        Uni<SelfHealingResponse> triggerSelfHealing(String runId, String nodeId,
                        String tenantId, String userId);

        /**
         * Escalate to human.
         * 
         * @param runId    Workflow run identifier
         * @param nodeId   Failed node identifier
         * @param request  Escalation request
         * @param tenantId Tenant identifier
         * @param userId   User identifier
         * @return Human task response
         */
        Uni<Object> escalateToHuman(String runId, String nodeId, EscalationRequest request,
                        String tenantId, String userId);

        // ========================================================================
        // STREAMING AND EVENTS
        // ========================================================================

        /**
         * Stream execution events.
         * 
         * @param runId    Workflow run identifier
         * @param tenantId Tenant identifier
         * @return Stream of execution events
         */
        Multi<WorkflowExecutionEvent> streamExecution(String runId, String tenantId);

        /**
         * Inject external event.
         * 
         * @param runId    Workflow run identifier
         * @param event    Event to inject
         * @param tenantId Tenant identifier
         * @param userId   User identifier
         * @return Void on success
         */
        Uni<Void> injectEvent(String runId, WorkflowEventRequest event,
                        String tenantId, String userId);

        // ========================================================================
        // WORKFLOW OUTPUT AND VARIABLES
        // ========================================================================

        /**
         * Get workflow output.
         * 
         * @param runId    Workflow run identifier
         * @param tenantId Tenant identifier
         * @return Workflow output
         */
        Uni<Map<String, Object>> getWorkflowOutput(String runId, String tenantId);

        /**
         * Update workflow variables.
         * 
         * @param runId     Workflow run identifier
         * @param variables Variables to update
         * @param tenantId  Tenant identifier
         * @param userId    User identifier
         * @return Void on success
         */
        Uni<Void> updateWorkflowVariables(String runId, Map<String, Object> variables,
                        String tenantId, String userId);

        // ========================================================================
        // QUERY AND ANALYTICS
        // ========================================================================

        /**
         * Query workflow runs.
         * 
         * @param query Workflow run query
         * @return Query results
         */
        Uni<PagedResponse<WorkflowRunSummary>> queryWorkflowRuns(WorkflowRunQuery query);

        /**
         * Get workflow metrics.
         * 
         * @param workflowId Workflow identifier
         * @param request    Metrics request
         * @param tenantId   Tenant identifier
         * @return Workflow metrics
         */
        Uni<WorkflowMetricsResponse> getWorkflowMetrics(String workflowId,
                        MetricsRequest request,
                        String tenantId);

        // ========================================================================
        // SIMULATION AND TESTING
        // ========================================================================

        /**
         * Simulate workflow execution.
         * 
         * @param request Unified workflow request
         * @return Simulation response
         */
        Uni<SimulationResponse> simulateWorkflow(UnifiedWorkflowRequest request);

        /**
         * Validate workflow execution.
         * 
         * @param request Unified workflow request
         * @return Validation response
         */
        Uni<Object> validateWorkflowExecution(UnifiedWorkflowRequest request);
}
