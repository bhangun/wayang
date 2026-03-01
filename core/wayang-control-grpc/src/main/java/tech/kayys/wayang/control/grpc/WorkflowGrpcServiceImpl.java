package tech.kayys.wayang.control.grpc;

import io.grpc.stub.StreamObserver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.service.WayangDefinitionService;
import tech.kayys.wayang.control.domain.WayangDefinition;
import tech.kayys.wayang.schema.DefinitionType;
import tech.kayys.wayang.schema.WayangSpec;

import java.util.UUID;

@ApplicationScoped
public class WorkflowGrpcServiceImpl extends WorkflowServiceGrpc.WorkflowServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(WorkflowGrpcServiceImpl.class);

    @Inject
    WayangDefinitionService definitionService;

    @Override
    public void createWorkflowTemplate(ControlPlaneProto.CreateWorkflowTemplateRequest request,
                                      StreamObserver<ControlPlaneProto.WorkflowTemplateResponse> responseObserver) {
        try {
            LOG.info("Creating workflow template (Definition): {}", request.getTemplateName());

            var projectId = UUID.fromString(request.getProjectId());
            
            WayangSpec spec = new WayangSpec();
            // In a real implementation, map request fields to spec

            var defUni = definitionService.create("default", projectId, request.getTemplateName(),
                request.getDescription(), DefinitionType.WORKFLOW_TEMPLATE, spec, "system");
            
            defUni.subscribe().with(
                def -> {
                    var response = ControlPlaneProto.WorkflowTemplateResponse.newBuilder()
                        .setSuccess(true)
                        .setTemplate(convertToProto(def))
                        .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error creating workflow definition", throwable);
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
            LOG.debug("Getting workflow definition: {}", request.getTemplateId());

            var defId = UUID.fromString(request.getTemplateId());

            var defUni = definitionService.findById(defId);
            
            defUni.subscribe().with(
                def -> {
                    if (def != null) {
                        var response = ControlPlaneProto.WorkflowTemplateResponse.newBuilder()
                            .setSuccess(true)
                            .setTemplate(convertToProto(def))
                            .build();
                        responseObserver.onNext(response);
                    } else {
                        var response = ControlPlaneProto.WorkflowTemplateResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Definition not found")
                            .build();
                        responseObserver.onNext(response);
                    }
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error getting workflow definition: " + request.getTemplateId(), throwable);
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
            LOG.debug("Listing workflow definitions for project: {}", request.getTenantId());

            // List by tenant/project if needed. For now using a placeholder projectId if present in request
            // or listing all. WayangDefinitionService.listByProject expects a UUID.
            
            responseObserver.onError(new UnsupportedOperationException("Listing definitions via gRPC needs projectId context"));
            
        } catch (Exception e) {
            LOG.error("Error in listWorkflowTemplates", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void publishWorkflowTemplate(ControlPlaneProto.PublishWorkflowTemplateRequest request,
                                       StreamObserver<ControlPlaneProto.PublishWorkflowTemplateResponse> responseObserver) {
        try {
            LOG.info("Publishing workflow definition: {}", request.getTemplateId());

            var defId = UUID.fromString(request.getTemplateId());

            var publishUni = definitionService.publish(defId, "system");
            
            publishUni.subscribe().with(
                def -> {
                    var response = ControlPlaneProto.PublishWorkflowTemplateResponse.newBuilder()
                        .setSuccess(true)
                        .setWorkflowDefinitionId(def.workflowDefinitionId)
                        .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error publishing workflow definition: " + request.getTemplateId(), throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in publishWorkflowTemplate", e);
            responseObserver.onError(e);
        }
    }

    private ControlPlaneProto.WorkflowTemplate convertToProto(WayangDefinition def) {
        var builder = ControlPlaneProto.WorkflowTemplate.newBuilder()
            .setTemplateId(def.definitionId.toString())
            .setProjectId(def.projectId.toString())
            .setTemplateName(def.name)
            .setDescription(def.description != null ? def.description : "")
            .setVersion(String.valueOf(def.versionNumber))
            .setTemplateType(def.definitionType.name())
            .setCanvasDefinition("") // Canvas data handled via Definition specs now
            .addAllTags(java.util.Collections.emptyList())
            .setCreatedAt(def.createdAt != null ? def.createdAt.toEpochMilli() : 0L)
            .setUpdatedAt(def.updatedAt != null ? def.updatedAt.toEpochMilli() : 0L)
            .setIsPublished("PUBLISHED".equals(def.status))
            .setWorkflowDefinitionId(def.workflowDefinitionId != null ? def.workflowDefinitionId : "");

        return builder.build();
    }
}
}