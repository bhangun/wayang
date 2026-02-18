package tech.kayys.wayang.control.dto;

import java.util.List;
import java.util.Map;

/**
 * Result of an AI agent task execution.
 */
public record AgentExecutionResult(
        String taskId,
        boolean success,
        String response,
        List<String> reasoningChain,
        Map<String, Object> metadata,
        List<String> errors) {
}
