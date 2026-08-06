package tech.kayys.wayang.event;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import java.util.Objects;

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Event Source - where the event originated
 */
public interface EventSource {
    Id id();
    ResourceType type();
    String name();
    
    static EventSource of(ResourceId id) {
        return new SimpleEventSource(id.value(), id.type(), id.asString());
    }
    
    static EventSource of(Id id, ResourceType type, String name) {
        return new SimpleEventSource(id, type, name);
    }
    
    record SimpleEventSource(
        Id id,
        ResourceType type,
        String name
    ) implements EventSource {
        public SimpleEventSource {
            Objects.requireNonNull(id, "id cannot be null");
            Objects.requireNonNull(type, "type cannot be null");
        }
    }
}
