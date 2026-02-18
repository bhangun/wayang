package tech.kayys.wayang.control.grpc;

import io.grpc.stub.StreamObserver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.service.ProjectManager;
import tech.kayys.wayang.control.dto.CreateProjectRequest;
import tech.kayys.wayang.control.domain.WayangProject;

import java.util.UUID;

@ApplicationScoped
public class ProjectGrpcServiceImpl extends ProjectServiceGrpc.ProjectServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectGrpcServiceImpl.class);

    @Inject
    ProjectManager projectManager;

    @Override
    public void createProject(ControlPlaneProto.CreateProjectRequest request,
                              StreamObserver<ControlPlaneProto.ProjectResponse> responseObserver) {
        try {
            LOG.info("Creating project: {}", request.getProjectName());

            // Convert gRPC request to internal DTO
            var createRequest = new tech.kayys.wayang.control.dto.CreateProjectRequest(
                request.getTenantId(),
                request.getProjectName(),
                request.getDescription(),
                tech.kayys.wayang.control.dto.ProjectType.valueOf(request.getProjectType()),
                request.getCreatedBy(),
                request.getMetadataMap()
            );

            // Call the service
            var projectUni = projectManager.createProject(createRequest);
            
            // Handle async response
            projectUni.subscribe().with(
                project -> {
                    var response = ControlPlaneProto.ProjectResponse.newBuilder()
                        .setSuccess(true)
                        .setProject(convertToProto(project))
                        .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error creating project", throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in createProject", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void getProject(ControlPlaneProto.GetProjectRequest request,
                           StreamObserver<ControlPlaneProto.ProjectResponse> responseObserver) {
        try {
            LOG.debug("Getting project: {}", request.getProjectId());

            var projectId = UUID.fromString(request.getProjectId());
            var tenantId = request.getTenantId();

            var projectUni = projectManager.getProject(projectId, tenantId);
            
            projectUni.subscribe().with(
                project -> {
                    if (project != null) {
                        var response = ControlPlaneProto.ProjectResponse.newBuilder()
                            .setSuccess(true)
                            .setProject(convertToProto(project))
                            .build();
                        responseObserver.onNext(response);
                    } else {
                        var response = ControlPlaneProto.ProjectResponse.newBuilder()
                            .setSuccess(false)
                            .setMessage("Project not found")
                            .build();
                        responseObserver.onNext(response);
                    }
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error getting project: " + request.getProjectId(), throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in getProject", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void listProjects(ControlPlaneProto.ListProjectsRequest request,
                             StreamObserver<ControlPlaneProto.ListProjectsResponse> responseObserver) {
        try {
            LOG.debug("Listing projects for tenant: {}", request.getTenantId());

            var tenantId = request.getTenantId();
            var projectType = request.hasProjectType() ? 
                tech.kayys.wayang.control.dto.ProjectType.valueOf(request.getProjectType()) : null;

            var projectsUni = projectManager.listProjects(tenantId, projectType);
            
            projectsUni.subscribe().with(
                projects -> {
                    var builder = ControlPlaneProto.ListProjectsResponse.newBuilder()
                        .setSuccess(true);
                    
                    for (var project : projects) {
                        builder.addProjects(convertToProto(project));
                    }
                    
                    responseObserver.onNext(builder.build());
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error listing projects for tenant: " + tenantId, throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in listProjects", e);
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteProject(ControlPlaneProto.DeleteProjectRequest request,
                              StreamObserver<ControlPlaneProto.DeleteProjectResponse> responseObserver) {
        try {
            LOG.info("Deleting project: {}", request.getProjectId());

            var projectId = UUID.fromString(request.getProjectId());
            var tenantId = request.getTenantId();

            var deleteUni = projectManager.deleteProject(projectId, tenantId);
            
            deleteUni.subscribe().with(
                success -> {
                    var response = ControlPlaneProto.DeleteProjectResponse.newBuilder()
                        .setSuccess(success)
                        .setMessage(success ? "Project deleted successfully" : "Project not found")
                        .build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                throwable -> {
                    LOG.error("Error deleting project: " + request.getProjectId(), throwable);
                    responseObserver.onError(throwable);
                }
            );
        } catch (Exception e) {
            LOG.error("Error in deleteProject", e);
            responseObserver.onError(e);
        }
    }

    // Helper method to convert internal project to proto
    private ControlPlaneProto.Project convertToProto(WayangProject project) {
        var builder = ControlPlaneProto.Project.newBuilder()
            .setProjectId(project.projectId.toString())
            .setTenantId(project.tenantId)
            .setProjectName(project.projectName)
            .setDescription(project.description != null ? project.description : "")
            .setProjectType(project.projectType != null ? project.projectType.name() : "")
            .setCreatedBy(project.createdBy != null ? project.createdBy : "")
            .setCreatedAt(project.createdAt != null ? project.createdAt.toEpochMilli() : 0L)
            .setUpdatedAt(project.updatedAt != null ? project.updatedAt.toEpochMilli() : 0L)
            .setIsActive(project.isActive);

        if (project.metadata != null) {
            builder.putAllMetadata(project.metadata);
        }

        return builder.build();
    }
}