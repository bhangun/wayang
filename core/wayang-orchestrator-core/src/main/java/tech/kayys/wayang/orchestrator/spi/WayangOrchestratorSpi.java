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
     * Deploys and executes a Wayang workflow/agent based on its definition spec.
     *
     * @param name   The name or ID of the execution instance.
     * @param spec   The full Wayang definition specification to execute.
     * @param inputs Initial inputs or variables to provide to the workflow.
     * @return The ID of the resulting execution or deployment.
     */
    Uni<String> execute(String name, WayangSpec spec, Map<String, Object> inputs);

    /**
     * Gets the status of a specific execution.
     *
     * @param executionId The ID returned by the execute method.
     * @return A status string (e.g., RUNNING, COMPLETED, FAILED).
     */
    Uni<String> getStatus(String executionId);

    /**
     * Stops an active execution.
     *
     * @param executionId The ID of the execution to stop.
     * @return True if stopped successfully, false otherwise.
     */
    Uni<Boolean> stop(String executionId);
}
