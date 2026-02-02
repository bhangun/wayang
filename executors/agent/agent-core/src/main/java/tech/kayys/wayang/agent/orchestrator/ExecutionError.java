package tech.kayys.wayang.agent.orchestrator;

import java.time.Instant;
import java.util.Map;

import tech.kayys.wayang.agent.ErrorSeverity;

/**
 * Execution Error
 */
public record ExecutionError(
        String errorCode,
        String message,
        ErrorSeverity severity,
        String agentId,
        Instant occurredAt,
        Map<String, Object> context) {
}
