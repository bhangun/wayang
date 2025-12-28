package tech.kayys.wayang.node.model;

import lombok.Builder;
import lombok.Data;
import tech.kayys.wayang.workflow.api.model.RunStatus;

import java.time.Instant;
import java.util.Map;

/**
 * Workflow execution run data.
 */
@Data
@Builder
public class WorkflowRun {
    private String id;
    private String workflowId;
    private String tenantId;
    private Instant startTime;
    private Map<String, Object> inputs;
    private RunStatus status;
    private CheckpointData checkpoint;
}
