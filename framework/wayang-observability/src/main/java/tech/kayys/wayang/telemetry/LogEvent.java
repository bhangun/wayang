package tech.kayys.wayang.telemetry;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.time.Instant;
import java.util.Map;

import tech.kayys.wayang.extension.Id;

/**
 * Log Event
 */
public record LogEvent(
    String id,
    LogLevel level,
    String message,
    Map<String, Object> attributes,
    Instant timestamp,
    Throwable throwable
) {
    public static LogEvent of(LogLevel level, String message) {
        return new LogEvent(
            Id.random().asString(),
            level,
            message,
            Map.of(),
            Instant.now(),
            null
        );
    }
}
