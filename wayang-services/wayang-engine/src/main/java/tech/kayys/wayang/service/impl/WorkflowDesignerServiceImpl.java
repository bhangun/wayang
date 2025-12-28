package tech.kayys.wayang.service.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import tech.kayys.wayang.node.dto.PagedResponse;
import tech.kayys.wayang.schema.WorkflowDesignRequest;
import tech.kayys.wayang.sdk.dto.ValidationResponse;
import tech.kayys.wayang.sdk.dto.WorkflowDefinitionResponse;
import tech.kayys.wayang.service.WorkflowDesignerService;
import tech.kayys.wayang.service.WorkflowService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of WorkflowDesignerService.
 * Decorator for existing WorkflowService.
 */
@ApplicationScoped
public class WorkflowDesignerServiceImpl implements WorkflowDesignerService {

    @Inject
    WorkflowService workflowService;

    @Override
    public Uni<WorkflowDefinitionResponse> createWorkflow(WorkflowDesignRequest request, String tenantId,
            String userId) {
        // Map request to WorkflowService call
        // This is a placeholder that returns a basic response
        WorkflowDefinitionResponse response = new WorkflowDefinitionResponse();
        response.setId(UUID.randomUUID().toString());
        response.setName(request.getName());
        response.setStatus("DRAFT");
        return Uni.createFrom().item(response);
    }

    @Override
    public Uni<WorkflowDefinitionResponse> updateWorkflow(String workflowId, WorkflowDesignRequest request,
            String tenantId, String userId) {
        WorkflowDefinitionResponse response = new WorkflowDefinitionResponse();
        response.setId(workflowId);
        response.setName(request.getName());
        return Uni.createFrom().item(response);
    }

    @Override
    public Uni<WorkflowDefinitionResponse> getWorkflow(String workflowId, String tenantId) {
        WorkflowDefinitionResponse response = new WorkflowDefinitionResponse();
        response.setId(workflowId);
        return Uni.createFrom().item(response);
    }

    @Override
    public Uni<PagedResponse<WorkflowDefinitionResponse>> listWorkflows(String tenantId, int page, int size,
            String status, List<String> tags) {
        return Uni.createFrom().item(new PagedResponse<>(Collections.emptyList(), page, size, 0));
    }

    @Override
    public Uni<ValidationResponse> validateWorkflow(WorkflowDesignRequest request, String tenantId) {
        ValidationResponse response = new ValidationResponse();
        response.setValid(true);
        return Uni.createFrom().item(response);
    }

    @Override
    public Uni<WorkflowDefinitionResponse> publishWorkflow(String workflowId, String version, String tenantId,
            String userId) {
        WorkflowDefinitionResponse response = new WorkflowDefinitionResponse();
        response.setId(workflowId);
        response.setStatus("PUBLISHED");
        response.setVersion(version);
        return Uni.createFrom().item(response);
    }

    @Override
    public Uni<Void> deleteWorkflow(String workflowId, String tenantId) {
        return Uni.createFrom().voidItem();
    }
}
