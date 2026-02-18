package tech.kayys.wayang.control.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.domain.WayangProject;
import tech.kayys.wayang.control.dto.CreateProjectRequest;
import tech.kayys.wayang.control.dto.ProjectType;
import tech.kayys.wayang.control.spi.ProjectManagerSpi;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing projects and workspaces.
 */
@ApplicationScoped
public class ProjectManager implements ProjectManagerSpi {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectManager.class);

    /**
     * Create a new project.
     */
    public Uni<WayangProject> createProject(CreateProjectRequest request) {
        LOG.info("Creating project: {} for tenant: {}", request.projectName(), request.tenantId());

        return Panache.withTransaction(() -> {
            WayangProject project = new WayangProject();
            project.tenantId = request.tenantId();
            project.projectName = request.projectName();
            project.description = request.description();
            project.projectType = request.projectType();
            project.createdAt = Instant.now();
            project.updatedAt = Instant.now();
            project.createdBy = request.createdBy();
            project.metadata = request.metadata() != null ? request.metadata() : new HashMap<>();

            return project.persist().map(p -> (WayangProject) p);
        });
    }

    /**
     * Get project by ID.
     */
    public Uni<WayangProject> getProject(UUID projectId, String tenantId) {
        return WayangProject.find("projectId = ?1 and tenantId = ?2 and isActive = true", projectId, tenantId)
                .firstResult();
    }

    /**
     * List projects for a tenant.
     */
    public Uni<List<WayangProject>> listProjects(String tenantId, ProjectType type) {
        String query = type != null ? "tenantId = ?1 and projectType = ?2 and isActive = true"
                : "tenantId = ?1 and isActive = true";
        return type != null ? WayangProject.list(query, tenantId, type) : WayangProject.list(query, tenantId);
    }

    /**
     * Delete/Archive a project.
     */
    public Uni<Boolean> deleteProject(UUID projectId, String tenantId) {
        return Panache.withTransaction(() -> WayangProject
                .update("isActive = false, updatedAt = ?1 where projectId = ?2 and tenantId = ?3", Instant.now(),
                        projectId, tenantId)
                .map(count -> count > 0));
    }
}
