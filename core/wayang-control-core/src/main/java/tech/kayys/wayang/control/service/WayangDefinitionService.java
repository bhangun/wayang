package tech.kayys.wayang.control.service;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.kayys.wayang.control.domain.WayangDefinition;
import tech.kayys.wayang.schema.DefinitionType;
import tech.kayys.wayang.schema.WayangSpec;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing WayangDefinitions — CRUD, versioning, and lifecycle.
 */
@ApplicationScoped
public class WayangDefinitionService {

    private static final Logger LOG = LoggerFactory.getLogger(WayangDefinitionService.class);

    /**
     * Create a new WayangDefinition.
     */
    public Uni<WayangDefinition> create(String tenantId, UUID projectId, String name,
            String description, DefinitionType type,
            WayangSpec spec, String createdBy) {
        LOG.info("Creating WayangDefinition '{}' in project {} for tenant {}", name, projectId, tenantId);

        WayangDefinition definition = new WayangDefinition();
        definition.tenantId = tenantId;
        definition.projectId = projectId;
        definition.name = name;
        definition.description = description;
        definition.definitionType = type;
        definition.spec = spec != null ? spec : new WayangSpec();
        definition.createdBy = createdBy;
        definition.createdAt = Instant.now();

        return Panache.withTransaction(() -> definition.persist().map(d -> (WayangDefinition) d));
    }

    /**
     * Get a definition by ID.
     */
    public Uni<WayangDefinition> findById(UUID definitionId) {
        return WayangDefinition.findById(definitionId);
    }

    /**
     * List all definitions for a project.
     */
    public Uni<List<WayangDefinition>> listByProject(UUID projectId) {
        return WayangDefinition.list("projectId", projectId);
    }

    /**
     * Update the spec (JSONB) of a definition.
     */
    public Uni<WayangDefinition> updateSpec(UUID definitionId, WayangSpec spec, String updatedBy) {
        LOG.info("Updating spec for WayangDefinition {}", definitionId);

        return Panache.withTransaction(() -> WayangDefinition.<WayangDefinition>findById(definitionId)
                .flatMap(def -> {
                    if (def == null) {
                        return Uni.createFrom().failure(
                                new IllegalArgumentException("Definition not found: " + definitionId));
                    }
                    if (def.isLocked) {
                        return Uni.createFrom().failure(
                                new IllegalStateException("Definition is locked by: " + def.lockedBy));
                    }
                    def.spec = spec;
                    def.updatedBy = updatedBy;
                    def.updatedAt = Instant.now();
                    def.versionNumber++;
                    return def.persist().map(d -> (WayangDefinition) d);
                }));
    }

    /**
     * Publish a definition (mark as ready for deployment).
     */
    public Uni<WayangDefinition> publish(UUID definitionId, String publishedBy) {
        LOG.info("Publishing WayangDefinition {}", definitionId);

        return Panache.withTransaction(() -> WayangDefinition.<WayangDefinition>findById(definitionId)
                .flatMap(def -> {
                    if (def == null) {
                        return Uni.createFrom().failure(
                                new IllegalArgumentException("Definition not found: " + definitionId));
                    }
                    def.status = "PUBLISHED";
                    def.publishedAt = Instant.now();
                    def.publishedBy = publishedBy;
                    return def.persist().map(d -> (WayangDefinition) d);
                }));
    }

    /**
     * Fork a definition (create a branch copy).
     */
    public Uni<WayangDefinition> fork(UUID definitionId, String branchName, String createdBy) {
        LOG.info("Forking WayangDefinition {} to branch '{}'", definitionId, branchName);

        return WayangDefinition.<WayangDefinition>findById(definitionId)
                .flatMap(original -> {
                    if (original == null) {
                        return Uni.createFrom().failure(
                                new IllegalArgumentException("Definition not found: " + definitionId));
                    }

                    WayangDefinition fork = new WayangDefinition();
                    fork.tenantId = original.tenantId;
                    fork.projectId = original.projectId;
                    fork.name = original.name + " (" + branchName + ")";
                    fork.description = original.description;
                    fork.definitionType = original.definitionType;
                    fork.spec = original.spec; // Deep copy via JSONB serialization
                    fork.branchName = branchName;
                    fork.parentDefinitionId = original.definitionId;
                    fork.createdBy = createdBy;
                    fork.createdAt = Instant.now();

                    return Panache.withTransaction(() -> fork.persist().map(d -> (WayangDefinition) d));
                });
    }

    /**
     * Delete a definition.
     */
    public Uni<Boolean> delete(UUID definitionId) {
        LOG.info("Deleting WayangDefinition {}", definitionId);
        return Panache.withTransaction(() -> WayangDefinition.deleteById(definitionId));
    }
}
