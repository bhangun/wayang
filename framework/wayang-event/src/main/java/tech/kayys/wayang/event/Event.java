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


import java.time.Instant;
import java.util.*;

import tech.kayys.wayang.identity.ResourceId.EventId;
import tech.kayys.wayang.identity.ResourceId.CorrelationId;
import tech.kayys.wayang.identity.ResourceId.CausationId;
import tech.kayys.wayang.resource.Resource;

/**
 * Event - the primary communication mechanism in Wayang.
 * Everything communicates through events.
 */
public interface Event extends Resource {
    
    EventId id();
    
    EventType eventType();
    
    Instant occurredAt();
    
    EventSource source();
    
    CorrelationId correlationId();
    
    CausationId causationId();
    
    EventPayload payload();
    
    EventMetadata eventMetadata();
    
    default String correlation() {
        return correlationId().asString();
    }
    
    default String causation() {
        return causationId().asString();
    }
}