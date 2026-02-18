package tech.kayys.wayang.control.dto;

import java.util.Map;

/**
 * Task definition to be executed by an AI agent.
 */
public record AgentTask(
        String taskId,
        String instruction,
        Map<String, Object> context,
        Map<String, Object> parameters) {
}
