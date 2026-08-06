package tech.kayys.wayang.audit;

import java.util.concurrent.CompletableFuture;
import java.util.List;

/**
 * Service for auditing and persisting agent artifacts (e.g., plan, task, implementation, walkthrough).
 * Implementations can store these in files, databases, or other backends.
 */
public interface AgentAuditService {

    /**
     * Saves an artifact to the audit storage.
     *
     * @param artifact The artifact to save.
     * @return A Uni representing the completion of the save operation.
     */
    CompletableFuture<Void> saveArtifact(AgentArtifact artifact);

    /**
     * Retrieves an artifact by its unique ID.
     *
     * @param artifactId The ID of the artifact.
     * @param tenantId The tenant ID for isolation.
     * @return A Uni containing the artifact, or null if not found.
     */
    CompletableFuture<AgentArtifact> getArtifact(String artifactId, String tenantId);

    /**
     * Retrieves all artifacts associated with a specific agent run.
     *
     * @param runId The ID of the agent execution run.
     * @param tenantId The tenant ID for isolation.
     * @return A Uni containing the list of artifacts for the run.
     */
    CompletableFuture<List<AgentArtifact>> getArtifactsByRun(String runId, String tenantId);
}
