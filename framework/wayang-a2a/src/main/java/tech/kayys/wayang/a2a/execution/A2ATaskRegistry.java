package tech.kayys.wayang.a2a.execution;

import java.util.Optional;

/**
 * Registry maintaining decoupled mappings between external A2A task IDs and internal execution IDs.
 */
public interface A2ATaskRegistry {

    void register(A2ATaskExecution execution);

    Optional<A2ATaskExecution> findByA2aTaskId(String a2aTaskId);

    Optional<A2ATaskExecution> findByExecutionId(String executionId);

    void remove(String a2aTaskId);
}
