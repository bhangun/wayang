package tech.kayys.wayang.coordination;

import io.smallrye.mutiny.Uni;

/**
 * Registry SPI for managing workflow definitions compiled from coordination plans.
 * Implementations ensure concurrency-safe definition reuse via fingerprinting.
 */
public interface CoordinationWorkflowDefinitionRegistry {

    /**
     * Retrieves an existing workflow definition matching the canonical fingerprint of the given definition,
     * or deploys a new definition if none exists.
     *
     * @param definition the coordination workflow definition IR
     * @param tenantId the tenant ID
     * @return a Uni emitting the unique workflow definition ID
     */
    Uni<String> getOrCreate(CoordinationWorkflowDefinition definition, String tenantId);

    /**
     * Convenience overload defaulting to "default" tenant.
     *
     * @param definition the coordination workflow definition IR
     * @return a Uni emitting the unique workflow definition ID
     */
    default Uni<String> getOrCreate(CoordinationWorkflowDefinition definition) {
        return getOrCreate(definition, "default");
    }
}
