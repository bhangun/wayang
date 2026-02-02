package tech.kayys.gamelan.executor.camel.modern;

import java.time.Instant;

record WebSocketMessage(
        String type,
        String sessionId,
        String content,
        String tenantId,
        Instant timestamp) {
}