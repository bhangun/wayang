package tech.kayys.wayang.control.spi;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.control.domain.WorkflowTemplate;
import tech.kayys.wayang.control.dto.CreateTemplateRequest;

import java.util.List;
import java.util.UUID;

/**
 * SPI interface for workflow management services.
 */
public interface WorkflowManagerSpi {

    /**
     * Create a new workflow template.
     */
    Uni<WorkflowTemplate> createWorkflowTemplate(UUID projectId, CreateTemplateRequest request);

    /**
     * Get a workflow template.
     */
    Uni<WorkflowTemplate> getWorkflowTemplate(UUID templateId);

    /**
     * Publish a workflow template to the orchestration engine.
     */
    Uni<String> publishWorkflowTemplate(UUID templateId);

    /**
     * List all workflow templates for a tenant.
     */
    Uni<List<WorkflowTemplate>> listAllTemplates(String tenantId);
}