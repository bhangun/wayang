package tech.kayys.wayang.execution.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import tech.kayys.wayang.execution.CheckpointStore;
import tech.kayys.wayang.execution.ExecutionSnapshot;

public class InMemoryCheckpointStore implements CheckpointStore {

    private final Map<UUID, ExecutionSnapshot> snapshots = new ConcurrentHashMap<>();

    @Override
    public void save(ExecutionSnapshot snapshot) {
        snapshots.put(snapshot.executionId(), snapshot);
    }

    @Override
    public ExecutionSnapshot restore(UUID executionId) {
        return snapshots.get(executionId);
    }

    @Override
    public void delete(UUID executionId) {
        snapshots.remove(executionId);
    }

    @Override
    public List<ExecutionSnapshot> list() {
        return new ArrayList<>(snapshots.values());
    }
}