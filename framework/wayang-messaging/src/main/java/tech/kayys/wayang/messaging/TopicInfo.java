package tech.kayys.wayang.messaging;
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
 * Topic Info
 */
public record TopicInfo(
    String name,
    int partitions,
    int replicationFactor,
    long messagesCount,
    long sizeBytes,
    Map<String, Object> metadata
) {}