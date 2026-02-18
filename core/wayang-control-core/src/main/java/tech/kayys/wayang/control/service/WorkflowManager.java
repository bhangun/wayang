package tech.kayys.wayang.control.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.domain.WayangProject;
import tech.kayys.wayang.control.domain.WorkflowTemplate;
import tech.kayys.wayang.control.dto.CreateTemplateRequest;
import tech.kayys.wayang.control.spi.WorkflowManagerSpi;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing workflow templates and definitions.
 */
@ApplicationScoped
public class WorkflowManager implements WorkflowManagerSpi {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowManager.class);

    /**
     * Create a new workflow template.
     */
    public Uni<WorkflowTemplate> createWorkflowTemplate(UUID projectId, CreateTemplateRequest request) {
        LOG.info("Creating workflow template: {} in project: {}", request.templateName(), projectId);

        return Panache.withTransaction(() -> WayangProject.<WayangProject>findById(projectId)
                .flatMap(project -> {
                    if (project == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Project not found"));
                    }
                    if (!project.isActive) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Project is inactive"));
                    }

                    WorkflowTemplate template = new WorkflowTemplate();
                    template.project = project;
                    template.templateName = request.templateName();
                    template.description = request.description();
                    template.version = request.version() != null ? request.version() : "1.0.0";
                    template.templateType = request.templateType();
                    template.canvasDefinition = request.canvasDefinition();
                    template.tags = request.tags();
                    template.createdAt = Instant.now();
                    template.updatedAt = Instant.now();
                    template.isPublished = false;

                    return template.persist().map(t -> (WorkflowTemplate) t);
                }));
    }

    /**
     * Get a workflow template.
     */
    public Uni<WorkflowTemplate> getWorkflowTemplate(UUID templateId) {
        return WorkflowTemplate.findById(templateId);
    }

    /**
     * Publish a workflow template to the orchestration engine.
     */
    public Uni<String> publishWorkflowTemplate(UUID templateId) {
        LOG.info("Publishing workflow template: {}", templateId);

        return WorkflowTemplate.<WorkflowTemplate>findById(templateId)
                .flatMap(template -> {
                    if (template == null) {
                        return Uni.createFrom().failure(new IllegalArgumentException("Template not found"));
                    }

                    return Panache.withTransaction(() -> {
                        String mockId = "def_" + UUID.randomUUID().toString();
                        template.workflowDefinitionId = mockId;
                        template.isPublished = true;
                        template.updatedAt = Instant.now();
                        return template.persist().map(t -> mockId);
                    });
                });
    }

    /**
     * List all workflow templates for a tenant.
     */
    public Uni<List<WorkflowTemplate>> listAllTemplates(String tenantId) {
        return WorkflowTemplate.find("project.tenantId = ?1", tenantId).list();
    }
}
