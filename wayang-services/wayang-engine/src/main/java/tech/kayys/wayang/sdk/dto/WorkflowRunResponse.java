package tech.kayys.wayang.sdk.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;

/**
 * SDK DTO for Workflow Run Response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkflowRunResponse {

    private String runId;
    private String workflowId;
    private String version;
    private RunStatus status;
    private String phase;
    private Instant createdAt;
    private Instant startTime;
    private Instant endTime;
    private Long duration;
    private int completedNodes;
    private int totalNodes;
    private int attempt;
    private int maxAttempts;
    private String error;
    private Map<String, Object> output;
    private Map<String, Object> metadata;
}
