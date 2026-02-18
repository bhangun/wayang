package tech.kayys.wayang.control.grpc;

import io.grpc.stub.StreamObserver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.service.WorkflowManager;
import tech.kayys.wayang.control.dto.CreateTemplateRequest;
import tech.kayys.wayang.control.domain.WorkflowTemplate;

import java.util.UUID;

@ApplicationScoped
public class WorkflowGrpcServiceImpl extends WorkflowServiceGrpc.WorkflowServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowGrpcServiceImpl.class);

    @Inject
    WorkflowManager workflowManager;

    @Override
    public void createWorkflowTemplate(ControlPlaneProto.CreateWorkflowTemplateRequest request,
                                      StreamObserver<ControlPlaneProto.WorkflowTemplateResponse> responseObserver) {
        try {
            LOG.info("Creating workflow template: {}", request.getTemplateName());

            var projectId = UUID.fromString(request.getProjectId());
            
            // Convert gRPC request to internal DTO
            var createRequest = new tech.kayys.wayang.control.dto.CreateTemplateRequest(
                request.getTemplateName(),
                request.getDescription(),
                request.getVersion(),
                tech.kayys.wayang.control.dto.TemplateType.valueOf(request.getTemplateType()),
                request.getCanvasDefinition(),
                request.getTagsList()
            );

            // Call the service
            var templateUni = workflowManager.createWorkflowTemplate(projectId, createRequest);
            
            // Handle async response
            templateUni.subscribe().with(
                template -> {
                    var response = ControlPlaneProto.WorkflowTemplateResponse.newBuilder()
                        .setSuccess(true)
                        .setTemplate(convertToProto(template))
                        .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error creating workflow template", throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in createWorkflowTemplate", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getWorkflowTemplate(ControlPlaneProto.GetWorkflowTemplateRequest request,
                                   StreamObserver<ControlPlaneProto.WorkflowTemplateResponse> responseObserver) {
        try {
            LOG.debug("Getting workflow template: {}", request.getTemplateId());

            var templateId = UUID.fromString(request.getTemplateId());

            var templateUni = workflowManager.getWorkflowTemplate(templateId);
            
            templateUni.subscribe().with(
                template -> {
                    if (template != null) {
                        var response = ControlPlaneProto.WorkflowTemplateResponse.newBuilder()
                            .setSuccess(true)
                            .setTemplate(convertToProto(template))
                            .build();
                        responseObserver.onNext(response);
                    } else {
                        var response = ControlPlaneProto.WorkflowTemplateResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Template not found")
                            .build();
                        responseObserver.onNext(response);
                    }
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error getting workflow template: " + request.getTemplateId(), throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in getWorkflowTemplate", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void listWorkflowTemplates(ControlPlaneProto.ListWorkflowTemplatesRequest request,
                                     StreamObserver<ControlPlaneProto.ListWorkflowTemplatesResponse> responseObserver) {
        try {
            LOG.debug("Listing workflow templates for tenant: {}", request.getTenantId());

            var tenantId = request.getTenantId();

            var templatesUni = workflowManager.listAllTemplates(tenantId);
            
            templatesUni.subscribe().with(
                templates -> {
                    var builder = ControlPlaneProto.ListWorkflowTemplatesResponse.newBuilder()
                        .setSuccess(true);
                    
                    for (var template : templates) {
                        builder.addTemplates(convertToProto(template));
                    }
                    
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error listing workflow templates for tenant: " + tenantId, throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in listWorkflowTemplates", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void publishWorkflowTemplate(ControlPlaneProto.PublishWorkflowTemplateRequest request,
                                       StreamObserver<ControlPlaneProto.PublishWorkflowTemplateResponse> responseObserver) {
        try {
            LOG.info("Publishing workflow template: {}", request.getTemplateId());

            var templateId = UUID.fromString(request.getTemplateId());

            var publishUni = workflowManager.publishWorkflowTemplate(templateId);
            
            publishUni.subscribe().with(
                workflowDefId -> {
                    var response = ControlPlaneProto.PublishWorkflowTemplateResponse.newBuilder()
                        .setSuccess(true)
                        .setWorkflowDefinitionId(workflowDefId)
                        .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error publishing workflow template: " + request.getTemplateId(), throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in publishWorkflowTemplate", e);
            responseObserver.onError(e);
        }
    }

    // Helper method to convert internal template to proto
    private ControlPlaneProto.WorkflowTemplate convertToProto(WorkflowTemplate template) {
        var builder = ControlPlaneProto.WorkflowTemplate.newBuilder()
            .setTemplateId(template.templateId.toString())
            .setProjectId(template.project.projectId.toString())
            .setTemplateName(template.templateName)
            .setDescription(template.description != null ? template.description : "")
            .setVersion(template.version != null ? template.version : "")
            .setTemplateType(template.templateType != null ? template.templateType.name() : "")
            .setCanvasDefinition(template.canvasDefinition != null ? template.canvasDefinition : "")
            .addAllTags(template.tags != null ? template.tags : java.util.Collections.emptyList())
            .setCreatedAt(template.createdAt != null ? template.createdAt.toEpochMilli() : 0L)
            .setUpdatedAt(template.updatedAt != null ? template.updatedAt.toEpochMilli() : 0L)
            .setIsPublished(template.isPublished)
            .setWorkflowDefinitionId(template.workflowDefinitionId != null ? template.workflowDefinitionId : "");

        return builder.build();
    }
}