package tech.kayys.wayang.inference;
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
import java.util.Set;

/**
 * Model Info
 */
public record ModelInfo(
    String id,
    String name,
    String provider,
    Set<String> capabilities,
    Map<String, Object> metadata
) {}