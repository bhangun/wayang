package tech.kayys.wayang.integration.core.model;

import java.time.Instant;
import java.util.Map;

public record DeadLetterMessage(
                String id,
                Object originalMessage,
                Object error,
                String runId,
                String nodeId,
                Instant timestamp,
                Instant expiresAt,
                Map<String, Object> errorDetails) {
}
