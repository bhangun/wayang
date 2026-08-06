package tech.kayys.wayang.configuration;
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
 * Configuration Change Event
 */
public record ConfigurationChangeEvent(
    ConfigId id,
    String path,
    Map<String, Object> oldValues,
    Map<String, Object> newValues,
    Instant timestamp
) {}