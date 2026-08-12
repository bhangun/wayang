package tech.kayys.wayang.memory;

import tech.kayys.wayang.project.ProjectContext;

/**
 * Defines the lifecycle operations for evaluating and managing memory candidates.
 */
public interface MemoryLifecycle {

    /**
     * Evaluates a candidate to determine if and how it should be stored.
     */
    boolean evaluate(ProjectContext project, MemoryCandidate candidate);

    /**
     * Persists an evaluated candidate into the appropriate storage tier.
     */
    void persist(ProjectContext project, MemoryCandidate candidate);

    /**
     * Archives a memory item, moving it to cold storage.
     */
    void archive(ProjectContext project, String memoryId);

    /**
     * Deletes a memory item permanently.
     */
    void delete(ProjectContext project, String memoryId);
}
