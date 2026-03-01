package tech.kayys.wayang.control.integration.designer;

import java.time.Instant;

public record DesignConnection(
        String connectionId,
        String sourceNodeId,
        String targetNodeId,
        String connectionType,
        String condition,
        Instant createdAt) {
}
