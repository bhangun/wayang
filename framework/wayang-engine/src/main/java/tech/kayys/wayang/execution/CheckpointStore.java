package tech.kayys.wayang.execution;

import java.util.List;
import java.util.UUID;

/**
 * Stores execution checkpoints for recovery and resumption.
 * 
 * <p>
 * Supports multiple storage backends:
 * <ul>
 * <li>In-memory (for testing)</li>
 * <li>Redis (for distributed execution)</li>
 * <li>PostgreSQL (for durable storage)</li>
 * <li>File system (for local storage)</li>
 * </ul>
 */
public interface CheckpointStore {

    /**
     * Saves an execution snapshot.
     */
    void save(ExecutionSnapshot snapshot);

    /**
     * Restores an execution snapshot.
     */
    ExecutionSnapshot restore(UUID executionId);

    /**
     * Deletes a checkpoint.
     */
    void delete(UUID executionId);

    /**
     * Returns all checkpoints.
     */
    List<ExecutionSnapshot> list();

    /**
     * Returns the latest checkpoint for an execution.
     */
    default ExecutionSnapshot getLatest(UUID executionId) {
        // Implementation would query by timestamp
        return null;
    }
}
