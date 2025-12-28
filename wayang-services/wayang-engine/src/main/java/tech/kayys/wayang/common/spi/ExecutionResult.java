package tech.kayys.wayang.common.spi;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Execution Result for Workflows and Nodes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {
    private String executionId;
    private String status;
    private boolean success;
    private Map<String, Object> output;
    private Object trace;
    private Map<String, Object> metadata;

    public static ExecutionResult success(String executionId, Map<String, Object> output) {
        return ExecutionResult.builder()
                .executionId(executionId)
                .status("COMPLETED")
                .success(true)
                .output(output)
                .build();
    }

    public static ExecutionResult error(String executionId, String message) {
        return ExecutionResult.builder()
                .executionId(executionId)
                .status("FAILED")
                .success(false)
                .metadata(Map.of("error", message))
                .build();
    }
}
