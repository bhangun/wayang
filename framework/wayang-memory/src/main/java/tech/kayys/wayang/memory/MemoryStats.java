package tech.kayys.wayang.memory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Map;

/**
 * Memory Stats
 */
public record MemoryStats(
    long totalRecords,
    long totalSizeBytes,
    Map<String, Long> typeCounts,
    Map<String, Long> userCounts
) {}