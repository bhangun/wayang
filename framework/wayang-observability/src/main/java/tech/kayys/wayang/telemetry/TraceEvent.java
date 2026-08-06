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

/**
 * Trace Event
 */
public record TraceEvent(
    String id,
    String name,
    Instant startTime,
    Instant endTime,
    Map<String, Object> attributes,
    String status
) {}
