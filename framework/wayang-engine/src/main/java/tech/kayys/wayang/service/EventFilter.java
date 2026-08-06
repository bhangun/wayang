package tech.kayys.wayang.service;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import tech.kayys.wayang.extension.Extension;
import tech.kayys.wayang.resource.Resource;
import tech.kayys.wayang.resource.BaseResource;


import tech.kayys.wayang.event.CorrelationId;
import tech.kayys.wayang.event.Event;
import tech.kayys.wayang.event.EventSource;
import tech.kayys.wayang.event.EventType;

/**
 * Event Filter
 */
@FunctionalInterface
public interface EventFilter {
    boolean matches(Event event);
    
    static EventFilter all() {
        return event -> true;
    }
    
    static EventFilter ofType(EventType type) {
        return event -> event.type().equals(type);
    }
    
    static EventFilter ofSource(EventSource source) {
        return event -> event.source().equals(source);
    }
    
    static EventFilter ofCorrelation(CorrelationId correlationId) {
        return event -> event.correlationId().equals(correlationId);
    }
}
