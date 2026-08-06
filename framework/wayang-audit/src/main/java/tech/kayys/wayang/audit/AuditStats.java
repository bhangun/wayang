package tech.kayys.wayang.audit;
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
 * Audit Stats
 */
public record AuditStats(
    long totalEvents,
    long successCount,
    long failureCount,
    long deniedCount,
    long errorCount,
    Map<String, Long> actionCounts,
    Map<String, Long> userCounts,
    Map<String, Long> targetCounts,
    Instant from,
    Instant to
) {}
