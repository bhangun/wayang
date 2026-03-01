package tech.kayys.wayang.orchestrator.spi;

import io.smallrye.mutiny.Uni;
import tech.kayys.wayang.schema.WayangSpec;

import java.util.Map;

/**
 * Service Provider Interface for Wayang Orchestration.
 * Defines the contract that must be implemented to execute a Wayang definition
 * (e.g. via Gamelan Local or Remote).
 */
public interface WayangOrchestratorSpi {

    /**
     * Deploy a Wayang definition to the orchestrator.
     * 
     * @param name Name for the definition
     * @param spec The Wayang specification
     * @return Unique definition ID
     */
    Uni<String> deploy(String name, WayangSpec spec);

    /**
     * Run a deployed definition.
     * 
     * @param definitionId The ID returned from deploy
     * @param inputs       Runtime inputs
     * @return Unique execution ID
     */
    Uni<String> run(String definitionId, Map<String, Object> inputs);

    /**
     * Get the status of an execution.
     */
    Uni<String> getStatus(String executionId);

    /**
     * Stop a running execution.
     */
    Uni<Boolean> stop(String executionId);
}
