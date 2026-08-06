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

import tech.kayys.wayang.extension.Id;
import tech.kayys.wayang.extension.Metadata;
import tech.kayys.wayang.identity.ResourceId;
import tech.kayys.wayang.identity.ResourceId.EventId;
import tech.kayys.wayang.resource.BaseResource;
import tech.kayys.wayang.resource.ResourceType;

/**
 * Base Event implementation
 */
public abstract class BaseEvent extends BaseResource implements Event {
    
    private final EventType type;
    private final Instant occurredAt;
    private final EventSource source;
    private final CorrelationId correlationId;
    private final CausationId causationId;
    private final EventPayload payload;
    private final EventMetadata eventMetadata;
    
    protected BaseEvent(Builder<?> builder) {
        super(
            builder.id != null ? builder.id : new ResourceId.EventId(Id.random()),
            builder.metadata != null ? builder.metadata : Metadata.builder()
                .name(builder.type != null ? builder.type.name() : "unknown-event")
                .label("type", builder.type != null ? builder.type.asString() : "unknown")
                .now()
                .build()
        );
        this.type = builder.type;
        this.occurredAt = builder.occurredAt != null ? builder.occurredAt : Instant.now();
        this.source = builder.source;
        this.correlationId = builder.correlationId;
        this.causationId = builder.causationId;
        this.payload = builder.payload;
        this.eventMetadata = builder.eventMetadata != null ? builder.eventMetadata : EventMetadata.empty();
    }
    
    @Override
    public EventId id() {
        return (EventId) super.id();
    }
    
    @Override
    public EventType type() { return type; }
    
    @Override
    public Instant occurredAt() { return occurredAt; }
    
    @Override
    public EventSource source() { return source; }
    
    @Override
    public CorrelationId correlationId() { return correlationId; }
    
    @Override
    public CausationId causationId() { return causationId; }
    
    @Override
    public EventPayload payload() { return payload; }
    
    @Override
    public EventMetadata eventMetadata() { return eventMetadata; }
    
    @Override
    public ResourceType type() {
        return new ResourceType.Event();
    }
    
    public static abstract class Builder<T extends Builder<T>> {
        private ResourceId.EventId id;
        private EventType type;
        private Instant occurredAt;
        private EventSource source;
        private CorrelationId correlationId;
        private CausationId causationId;
        private EventPayload payload;
        private EventMetadata eventMetadata;
        private Metadata metadata;
        
        @SuppressWarnings("unchecked")
        public T id(ResourceId.EventId id) {
            this.id = id;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T type(EventType type) {
            this.type = type;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T occurredAt(Instant occurredAt) {
            this.occurredAt = occurredAt;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T source(EventSource source) {
            this.source = source;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T correlationId(CorrelationId correlationId) {
            this.correlationId = correlationId;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T correlationId(String correlationId) {
            this.correlationId = new CorrelationId(Id.fromString(correlationId));
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T causationId(CausationId causationId) {
            this.causationId = causationId;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T causationId(String causationId) {
            this.causationId = new CausationId(Id.fromString(causationId));
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T payload(EventPayload payload) {
            this.payload = payload;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T eventMetadata(EventMetadata eventMetadata) {
            this.eventMetadata = eventMetadata;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T metadata(Metadata metadata) {
            this.metadata = metadata;
            return (T) this;
        }
        
        public abstract Event build();
    }
}