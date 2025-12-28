package tech.kayys.wayang.node.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.Map;

/**
 * Result of a Human-in-the-Loop task.
 */
@Data
@Builder
@Jacksonized
public class HTILTaskResult {
    private String taskId;
    private String nodeId;
    private String decision;
    private Map<String, Object> inputs;
    private Instant completedAt;
    private String completedBy;
}
