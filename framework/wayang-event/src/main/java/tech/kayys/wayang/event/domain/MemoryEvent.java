package tech.kayys.wayang.event.domain;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.List;

import tech.kayys.wayang.event.EventPayload;

/**
 * Memory Events
 */
public interface MemoryEvent extends EventPayload {
    
    record MemoryLoaded(
        String memoryId,
        String type,
        List<String> keys,
        int count
    ) implements MemoryEvent {}
    
    record MemoryStored(
        String memoryId,
        String type,
        String key,
        long size
    ) implements MemoryEvent {}
    
    record MemoryEvicted(
        String memoryId,
        String type,
        String key,
        String reason
    ) implements MemoryEvent {}
    
    record MemorySearchCompleted(
        String memoryId,
        String query,
        int results,
        long durationMs
    ) implements MemoryEvent {}
}
