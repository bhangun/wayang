package tech.kayys.wayang.websocket;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for CollaborationHandlers to bridge between connection handles and
 * logic
 */
@ApplicationScoped
public class CollaborationHandlerRegistry {

    private final Map<String, CollaborationHandler> handlers = new ConcurrentHashMap<>();

    public void register(String handlerId, CollaborationHandler handler) {
        handlers.put(handlerId, handler);
    }

    public CollaborationHandler get(String handlerId) {
        return handlers.get(handlerId);
    }

    public CollaborationHandler remove(String handlerId) {
        return handlers.remove(handlerId);
    }
}
