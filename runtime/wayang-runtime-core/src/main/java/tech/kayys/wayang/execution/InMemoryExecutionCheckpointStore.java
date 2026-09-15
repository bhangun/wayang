package tech.kayys.wayang.execution;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory lifecycle checkpoint implementation.
 *
 * <p>This implementation is suitable for standalone/community mode and tests.
 * A persistent implementation will replace it for server deployments.</p>
 */
@ApplicationScoped
public class InMemoryExecutionCheckpointStore
    implements ExecutionCheckpointStore {

    private final Map<String, List<ExecutionCheckpoint>> checkpoints =
        new ConcurrentHashMap<>();

    @Override
    public void save(ExecutionCheckpoint checkpoint) {
        checkpoints
            .computeIfAbsent(
                checkpoint.executionId(),
                ignored -> new CopyOnWriteArrayList<>()
            )
            .add(checkpoint);
    }

    @Override
    public Optional<ExecutionCheckpoint> load(String executionId) {
        List<ExecutionCheckpoint> history =
            checkpoints.get(executionId);

        if (history == null || history.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
            history.get(history.size() - 1)
        );
    }

    @Override
    public Optional<ExecutionCheckpoint> load(
        String executionId,
        ExecutionCheckpointId checkpointId
    ) {
        List<ExecutionCheckpoint> history =
            checkpoints.get(executionId);

        if (history == null) {
            return Optional.empty();
        }

        return history.stream()
            .filter(checkpoint ->
                checkpoint.checkpointId().equals(checkpointId)
            )
            .findFirst();
    }

    @Override
    public List<ExecutionCheckpoint> history(
        String executionId
    ) {
        List<ExecutionCheckpoint> history =
            checkpoints.get(executionId);

        if (history == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(history);
    }

    @Override
    public void delete(String executionId) {
        checkpoints.remove(executionId);
    }
}
