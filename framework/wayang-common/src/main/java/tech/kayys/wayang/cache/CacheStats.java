package tech.kayys.wayang.cache;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


/**
 * Cache Stats
 */
public record CacheStats(
    long hits,
    long misses,
    long evictions,
    long size
) {}
