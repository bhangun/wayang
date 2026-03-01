package tech.kayys.wayang.control.integration.designer;

import java.time.Instant;

public record GeneratedRoute(
        String routeId,
        String sourceCode,
        String language,
        Instant generatedAt) {
}
