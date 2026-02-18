package tech.kayys.wayang.agent.orchestrator;

import java.time.Instant;
import java.util.Map;

import tech.kayys.wayang.error.ErrorCode;
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

    public static ExecutionError from(ErrorCode errorCode, String message, ErrorSeverity severity,
            String agentId, Map<String, Object> context) {
        return new ExecutionError(
                errorCode.getCode(),
                message != null ? message : errorCode.getDefaultMessage(),
                severity,
                agentId,
                Instant.now(),
                context);
    }
}
