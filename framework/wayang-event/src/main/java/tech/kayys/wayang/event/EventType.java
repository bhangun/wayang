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

/**
 * Event Type
 */
public interface EventType {
    String namespace();
    String name();
    int version();
    
    default String asString() {
        return namespace() + "." + name();
    }
    
    static EventType of(String namespace, String name) {
        return new SimpleEventType(namespace, name, 1);
    }
    
    static EventType of(String namespace, String name, int version) {
        return new SimpleEventType(namespace, name, version);
    }
    
    record SimpleEventType(
        String namespace,
        String name,
        int version
    ) implements EventType {
        public SimpleEventType {
            Objects.requireNonNull(namespace, "namespace cannot be null");
            Objects.requireNonNull(name, "name cannot be null");
            if (version < 1) {
                throw new IllegalArgumentException("version must be >= 1");
            }
        }
    }
}