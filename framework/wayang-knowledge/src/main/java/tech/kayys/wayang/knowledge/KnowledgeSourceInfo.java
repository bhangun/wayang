package tech.kayys.wayang.knowledge;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Set;

/**
 * Knowledge Source Info
 */
public record KnowledgeSourceInfo(
    String name,
    String type,
    Set<String> capabilities
) {}
