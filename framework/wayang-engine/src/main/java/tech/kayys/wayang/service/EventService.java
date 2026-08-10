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


import java.util.EventListener;
import java.util.List;

import javax.xml.stream.EventFilter;

import tech.kayys.wayang.identity.ResourceId.CorrelationId;
import tech.kayys.wayang.event.Event;
import tech.kayys.wayang.event.EventSource;
import tech.kayys.wayang.event.EventType;

/**
 * Event Service - manages events
 */
public interface EventService {
    void publish(Event event);
    void subscribe(EventFilter filter, EventListener listener);
    void unsubscribe(EventFilter filter, EventListener listener);
    
    <T extends Event> List<T> queryEvents(EventFilter filter);
    List<Event> getEvents(CorrelationId correlationId);
    List<Event> getEvents(EventSource source);
    List<Event> getEvents(EventType type);
    
    void replay(CorrelationId correlationId);
    void storeEvent(Event event);
}

