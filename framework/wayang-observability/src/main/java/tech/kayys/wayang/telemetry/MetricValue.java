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


import java.util.Map;

/**
 * Metric Value
 */
public record MetricValue(
    String name,
    MetricType type,
    double value,
    long count,
    double min,
    double max,
    double mean,
    double p50,
    double p95,
    double p99,
    Map<String, String> tags
) {}
