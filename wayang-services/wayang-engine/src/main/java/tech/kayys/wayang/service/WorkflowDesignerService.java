package tech.kayys.wayang.service;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.node.dto.PagedResponse;
import tech.kayys.wayang.schema.WorkflowDesignRequest;
import tech.kayys.wayang.sdk.dto.ValidationResponse;
import tech.kayys.wayang.sdk.dto.WorkflowDefinitionResponse;

import java.util.List;

/**
 * Workflow Designer Service Interface.
 * 
 * Provides operations for workflow design and management:
 * - Create, update, delete workflows
 * - Validate workflow definitions
 * - Publish workflows
 * - List and search workflows
 * 
 * @since 1.0.0
 */
public interface WorkflowDesignerService {

    /**
     * Create a new workflow definition.
     * 
     * @param request  Workflow creation request
     * @param tenantId Tenant identifier
     * @param userId   User identifier
     * @return Created workflow
     */
    Uni<WorkflowDefinitionResponse> createWorkflow(
            WorkflowDesignRequest request, String tenantId, String userId);

    /**
     * Update existing workflow definition.
     * 
     * @param workflowId Workflow identifier
     * @param request    Updated workflow definition
     * @param tenantId   Tenant identifier
     * @param userId     User identifier
     * @return Updated workflow
     */
    Uni<WorkflowDefinitionResponse> updateWorkflow(
            String workflowId, WorkflowDesignRequest request, String tenantId, String userId);

    /**
     * Get workflow definition by ID.
     * 
     * @param workflowId Workflow identifier
     * @param tenantId   Tenant identifier
     * @return Workflow definition
     */
    Uni<WorkflowDefinitionResponse> getWorkflow(String workflowId, String tenantId);

    /**
     * List all workflows for tenant.
     * 
     * @param tenantId Tenant identifier
     * @param page     Page number
     * @param size     Page size
     * @param status   Filter by status (optional)
     * @param tags     Filter by tags (optional)
     * @return Paginated workflow list
     */
    Uni<PagedResponse<WorkflowDefinitionResponse>> listWorkflows(
            String tenantId, int page, int size, String status, List<String> tags);

    /**
     * Validate workflow definition.
     * 
     * @param request  Workflow to validate
     * @param tenantId Tenant identifier
     * @return Validation result
     */
    Uni<ValidationResponse> validateWorkflow(WorkflowDesignRequest request, String tenantId);

    /**
     * Publish workflow to make it executable.
     * 
     * @param workflowId Workflow identifier
     * @param version    Version to publish
     * @param tenantId   Tenant identifier
     * @param userId     User identifier
     * @return Published workflow
     */
    Uni<WorkflowDefinitionResponse> publishWorkflow(
            String workflowId, String version, String tenantId, String userId);

    /**
     * Delete workflow definition.
     * 
     * @param workflowId Workflow identifier
     * @param tenantId   Tenant identifier
     * @return Void on success
     */
    Uni<Void> deleteWorkflow(String workflowId, String tenantId);
}
