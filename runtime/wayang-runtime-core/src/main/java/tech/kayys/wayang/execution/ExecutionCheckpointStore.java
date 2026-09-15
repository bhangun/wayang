package tech.kayys.wayang.execution;

import java.util.List;
import java.util.Optional;

/**
 * Durable lifecycle checkpoint repository.
 *
 * <p>This is separate from the existing AgentContext-oriented
 * {@link CheckpointStore} so existing integrations remain compatible.</p>
 */
public interface ExecutionCheckpointStore {

    void save(ExecutionCheckpoint checkpoint);

    Optional<ExecutionCheckpoint> load(String executionId);

    Optional<ExecutionCheckpoint> load(
        String executionId,
        ExecutionCheckpointId checkpointId
    );

    List<ExecutionCheckpoint> history(String executionId);

    void delete(String executionId);
}
