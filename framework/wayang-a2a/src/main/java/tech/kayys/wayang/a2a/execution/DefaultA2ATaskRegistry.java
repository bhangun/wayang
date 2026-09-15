package tech.kayys.wayang.a2a.execution;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultA2ATaskRegistry implements A2ATaskRegistry {

    private final ConcurrentMap<String, A2ATaskExecution> byA2aId = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> execToA2a = new ConcurrentHashMap<>();

    @Override
    public void register(A2ATaskExecution execution) {
        Objects.requireNonNull(execution, "execution");
        byA2aId.put(execution.a2aTaskId(), execution);
        execToA2a.put(execution.executionId(), execution.a2aTaskId());
    }

    @Override
    public Optional<A2ATaskExecution> findByA2aTaskId(String a2aTaskId) {
        if (a2aTaskId == null) return Optional.empty();
        return Optional.ofNullable(byA2aId.get(a2aTaskId));
    }

    @Override
    public Optional<A2ATaskExecution> findByExecutionId(String executionId) {
        if (executionId == null) return Optional.empty();
        String a2aId = execToA2a.get(executionId);
        if (a2aId == null) return Optional.empty();
        return findByA2aTaskId(a2aId);
    }

    @Override
    public void remove(String a2aTaskId) {
        if (a2aTaskId == null) return;
        A2ATaskExecution removed = byA2aId.remove(a2aTaskId);
        if (removed != null) {
            execToA2a.remove(removed.executionId());
        }
    }
}
