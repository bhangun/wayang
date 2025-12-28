package tech.kayys.wayang.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.wayang.domain.Workspace;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * WorkspaceRepository - Multi-tenant workspace data access
 */
@ApplicationScoped
public class WorkspaceRepository implements PanacheRepositoryBase<Workspace, UUID> {

        /**
         * Find all non-deleted workspaces for tenant
         */
        public Uni<List<Workspace>> findByTenant(String tenantId) {
                return find("tenantId = :tenantId and status != :deleted",
                                Parameters.with("tenantId", tenantId)
                                                .and("deleted", Workspace.WorkspaceStatus.DELETED))
                                .list();
        }

        /**
         * Find workspace by ID with tenant validation
         */
        public Uni<Workspace> findByIdAndTenant(UUID id, String tenantId) {
                return find("id = :id and tenantId = :tenantId and status != :deleted",
                                Parameters.with("id", id)
                                                .and("tenantId", tenantId)
                                                .and("deleted", Workspace.WorkspaceStatus.DELETED))
                                .firstResult();
        }

        /**
         * Find workspace by ID with tenant validation (optional version)
         */
        public Uni<Optional<Workspace>> findByIdAndTenantOptional(UUID id, String tenantId) {
                return find("id = :id and tenantId = :tenantId and status != :deleted",
                                Parameters.with("id", id)
                                                .and("tenantId", tenantId)
                                                .and("deleted", Workspace.WorkspaceStatus.DELETED))
                                .firstResult()
                                .map(Optional::ofNullable);
        }

        /**
         * Find all workspaces for tenant with pagination
         */
        public Uni<List<Workspace>> findByTenantPaginated(String tenantId, int page, int size) {
                return find("tenantId = :tenantId and status != :deleted order by updatedAt desc",
                                Parameters.with("tenantId", tenantId)
                                                .and("deleted", Workspace.WorkspaceStatus.DELETED))
                                .page(page, size)
                                .list();
        }

        /**
         * Find workspaces by owner with pagination
         */
        public Uni<List<Workspace>> findByOwner(String tenantId, String ownerId, int page, int size) {
                return find("tenantId = :tenantId and ownerId = :ownerId and status != :deleted order by updatedAt desc",
                                Parameters.with("tenantId", tenantId)
                                                .and("ownerId", ownerId)
                                                .and("deleted", Workspace.WorkspaceStatus.DELETED))
                                .page(page, size)
                                .list();
        }

        /**
         * Search workspaces by name with pagination
         */
        public Uni<List<Workspace>> searchByName(String tenantId, String namePattern, int page, int size) {
                return find("tenantId = :tenantId and lower(name) like :pattern and status != :deleted order by name",
                                Parameters.with("tenantId", tenantId)
                                                .and("pattern", "%" + namePattern.toLowerCase() + "%")
                                                .and("deleted", Workspace.WorkspaceStatus.DELETED))
                                .page(page, size)
                                .list();
        }

        /**
         * Count workspaces by tenant
         */
        public Uni<Long> countByTenant(String tenantId) {
                return count("tenantId = :tenantId and status != :deleted",
                                Parameters.with("tenantId", tenantId)
                                                .and("deleted", Workspace.WorkspaceStatus.DELETED));
        }

        /**
         * Check if workspace name exists for tenant
         */
        public Uni<Boolean> existsByNameAndTenant(String name, String tenantId) {
                return count("name = :name and tenantId = :tenantId and status != :deleted",
                                Parameters.with("name", name)
                                                .and("tenantId", tenantId)
                                                .and("deleted", Workspace.WorkspaceStatus.DELETED))
                                .map(count -> count > 0);
        }

        /**
         * Soft delete workspace
         */
        public Uni<Boolean> softDelete(UUID id, String tenantId) {
                return update("status = :deleted, updatedAt = :now where id = :id and tenantId = :tenantId",
                                Parameters.with("deleted", Workspace.WorkspaceStatus.DELETED)
                                                .and("now", Instant.now())
                                                .and("id", id)
                                                .and("tenantId", tenantId))
                                .map(count -> count > 0);
        }

        /**
         * Find workspaces with workflow count
         */
        public Uni<List<Object[]>> findWithWorkflowCount(String tenantId) {
                return getSession().flatMap(session -> session
                                .createQuery(
                                                "SELECT w, (SELECT COUNT(wf) FROM Workflow wf WHERE wf.workspace.id = w.id AND wf.tenantId = :tenantId AND wf.status != :deleted) "
                                                                +
                                                                "FROM Workspace w WHERE w.tenantId = :tenantId AND w.status != :deleted "
                                                                +
                                                                "ORDER BY w.updatedAt DESC",
                                                Object[].class)
                                .setParameter("tenantId", tenantId)
                                .setParameter("deleted", Workspace.WorkspaceStatus.DELETED)
                                .getResultList());
        }
}
