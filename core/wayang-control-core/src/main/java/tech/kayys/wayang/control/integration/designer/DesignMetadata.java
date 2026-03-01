package tech.kayys.wayang.control.integration.designer;

import java.time.Instant;
import java.util.Map;

public record DesignMetadata(
        Instant createdAt,
        Instant updatedAt,
        String version,
        String status,
        Map<String, Object> tags) {
}
