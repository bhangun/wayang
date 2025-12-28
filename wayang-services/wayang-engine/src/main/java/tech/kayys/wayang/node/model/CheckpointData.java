package tech.kayys.wayang.node.model;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import tech.kayys.wayang.schema.ExecutionError;

/**
 * Checkpoint data for persistence.
 */
@Data
@Builder
public class CheckpointData {
    private Map<String, NodeExecutionResult> nodeResults;
    private Map<String, NodeState> nodeStates;
    private Map<String, Object> dataFlow;
    private List<ExecutionError> errorHistory;
    private List<HTILTaskResult> humanDecisions;
    private Map<String, Object> metadata;
    private boolean awaitingHuman;
    private String humanTaskId;
}