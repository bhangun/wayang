package tech.kayys.wayang.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Uni;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * Workflow - Complete workflow definition with logic and UI layers.
 */
@Entity
@Table(name = "workflows", indexes = {
        @Index(name = "idx_workflow_workspace", columnList = "workspace_id"),
        @Index(name = "idx_workflow_tenant", columnList = "tenant_id"),
        @Index(name = "idx_workflow_version", columnList = "version")
})
public class Workflow extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    public Workspace workspace;

    @Column(nullable = false, length = 128)
    public String name;

    @Column(length = 2000)
    public String description;

    @Column(name = "tenant_id", nullable = false)
    public String tenantId;

    @Column(nullable = false, length = 32)
    public String version;

    @Column(name = "created_by", nullable = false)
    public String createdBy;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at")
    public Instant updatedAt;

    @Column(name = "published_at")
    public Instant publishedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public WorkflowStatus status = WorkflowStatus.DRAFT;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "definition", columnDefinition = "jsonb")
    public tech.kayys.wayang.schema.workflow.WorkflowDefinition definition;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public Map<String, Object> metadata;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    public List<String> tags;

    @Column(name = "is_locked")
    public boolean locked;

    @Column(name = "locked_by")
    public String lockedBy;

    @Column(name = "locked_at")
    public Instant lockedAt;

    @Column(name = "last_modified_by")
    public String lastModifiedBy;

    @Version
    public Long entityVersion; // Optimistic locking

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (metadata == null)
            metadata = new HashMap<>();

        // Sync basic fields to definition if present
        if (definition != null) {
            if (definition.getId() == null)
                definition.setId(id.toString());
            if (definition.getName() == null)
                definition.setName(name);
            if (definition.getTenantId() == null)
                definition.setTenantId(tenantId);
            if (definition.getCreatedBy() == null)
                definition.setCreatedBy(createdBy);
            if (definition.getCreatedAt() == null)
                definition.setCreatedAt(createdAt.toString());
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        if (definition != null) {
            definition.setVersion(version); // Keep entity version in sync if needed
        }
    }

    public enum WorkflowStatus {
        DRAFT, // Being edited
        VALIDATING, // Validation in progress
        VALID, // Passed validation
        INVALID, // Failed validation
        PUBLISHED, // Immutable published version
        ARCHIVED, // Historical version
        DELETED // Soft deleted
    }

    // Domain logic
    public boolean canPublish() {
        return status == WorkflowStatus.VALID;
    }

    public boolean isPublished() {
        return status == WorkflowStatus.PUBLISHED;
    }

    // Multi-tenant queries
    public static Uni<List<Workflow>> findByWorkspace(UUID workspaceId, String tenantId) {
        return find("workspace.id = ?1 and tenantId = ?2 and status != ?3",
                workspaceId, tenantId, WorkflowStatus.DELETED)
                .list();
    }

    public static Uni<Workflow> findLatestVersion(UUID workspaceId, String name, String tenantId) {
        return find("workspace.id = ?1 and name = ?2 and tenantId = ?3 " +
                "and status != ?4 order by createdAt desc",
                workspaceId, name, tenantId, WorkflowStatus.DELETED)
                .firstResult();
    }
}
