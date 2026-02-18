package tech.kayys.gamelan.executor.camel.modern;

import java.time.Instant;

record SSEEvent(
        String id,
        String type,
        String data,
        String tenantId,
        Instant timestamp) {
}